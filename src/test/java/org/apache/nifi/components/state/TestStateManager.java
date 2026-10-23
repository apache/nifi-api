/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.components.state;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestStateManager {

    private static final String FIRST_KEY = "first";
    private static final String SECOND_KEY = "second";
    private static final String THIRD_KEY = "third";
    private static final String MISSING_KEY = "missing";
    private static final String OTHER_WRITER_KEY = "otherWriter";

    private static final String FIRST_VALUE = "1";
    private static final String SECOND_VALUE = "2";
    private static final String EMPTY_VALUE = "";

    @Test
    void testPatchState() throws Exception {
        final InMemoryStateManager stateManager = new InMemoryStateManager();

        assertThrows(NullPointerException.class, () -> stateManager.patchState(null, Scope.LOCAL));
        stateManager.patchState(Map.of(), Scope.LOCAL);
        assertEquals(0, stateManager.replaceCount);
        assertTrue(stateManager.getState(Scope.LOCAL).getStateVersion().isEmpty());

        final Map<String, String> firstPatch = new HashMap<>();
        firstPatch.put(FIRST_KEY, FIRST_VALUE);
        firstPatch.put(SECOND_KEY, null);
        firstPatch.put(THIRD_KEY, EMPTY_VALUE);
        stateManager.patchState(firstPatch, Scope.LOCAL);
        final StateMap firstState = stateManager.getState(Scope.LOCAL);
        assertEquals(Map.of(FIRST_KEY, FIRST_VALUE, THIRD_KEY, EMPTY_VALUE), firstState.toMap());
        assertTrue(firstState.getStateVersion().isPresent());

        final Map<String, String> secondPatch = new HashMap<>();
        secondPatch.put(FIRST_KEY, SECOND_VALUE);
        secondPatch.put(MISSING_KEY, null);
        stateManager.patchState(secondPatch, Scope.LOCAL);
        assertEquals(Map.of(FIRST_KEY, SECOND_VALUE, THIRD_KEY, EMPTY_VALUE), stateManager.getState(Scope.LOCAL).toMap());

        final Map<String, String> thirdPatch = new HashMap<>();
        thirdPatch.put(THIRD_KEY, null);
        stateManager.patchState(thirdPatch, Scope.LOCAL);
        assertEquals(Map.of(FIRST_KEY, SECOND_VALUE), stateManager.getState(Scope.LOCAL).toMap());
        assertEquals(3, stateManager.replaceCount);
    }

    @Test
    void testPatchStateRetriesAfterFailedReplace() throws Exception {
        final InMemoryStateManager stateManager = new InMemoryStateManager();
        stateManager.setState(Map.of(FIRST_KEY, FIRST_VALUE), Scope.LOCAL);
        stateManager.writeBeforeNextReplace = Map.of(OTHER_WRITER_KEY, FIRST_VALUE);

        stateManager.patchState(Map.of(SECOND_KEY, SECOND_VALUE), Scope.LOCAL);

        assertEquals(Map.of(FIRST_KEY, FIRST_VALUE, SECOND_KEY, SECOND_VALUE, OTHER_WRITER_KEY, FIRST_VALUE), stateManager.getState(Scope.LOCAL).toMap());
        assertEquals(2, stateManager.replaceCount);
    }

    private record SimpleStateMap(Map<String, String> values, Optional<String> version) implements StateMap {
        @Override
        public Optional<String> getStateVersion() {
            return version;
        }

        @Override
        public String get(final String key) {
            return values.get(key);
        }

        @Override
        public Map<String, String> toMap() {
            return values;
        }
    }

    /**
     * Holds a single state map for all scopes. When {@code writeBeforeNextReplace} is set, the next call to
     * {@link #replace(StateMap, Map, Scope)} first stores those keys, as another writer would, and then fails because
     * the state no longer matches the caller's old value.
     */
    private static class InMemoryStateManager implements StateManager {
        private StateMap currentState = new SimpleStateMap(Map.of(), Optional.empty());
        private long version = 0;
        private int replaceCount = 0;
        private Map<String, String> writeBeforeNextReplace;

        @Override
        public void setState(final Map<String, String> state, final Scope scope) {
            currentState = new SimpleStateMap(Map.copyOf(state), Optional.of(String.valueOf(++version)));
        }

        @Override
        public StateMap getState(final Scope scope) {
            return currentState;
        }

        @Override
        public boolean replace(final StateMap oldValue, final Map<String, String> newValue, final Scope scope) {
            replaceCount++;
            if (writeBeforeNextReplace != null) {
                final Map<String, String> otherWriterState = new HashMap<>(currentState.toMap());
                otherWriterState.putAll(writeBeforeNextReplace);
                writeBeforeNextReplace = null;
                setState(otherWriterState, scope);
            }

            if (oldValue != currentState) {
                return false;
            }

            setState(newValue, scope);
            return true;
        }

        @Override
        public void clear(final Scope scope) {
            setState(Map.of(), scope);
        }
    }
}
