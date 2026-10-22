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

public class TestStateManager {

    @Test
    public void testPatchState() throws Exception {
        final InMemoryStateManager stateManager = new InMemoryStateManager();

        assertThrows(NullPointerException.class, () -> stateManager.patchState(null, Scope.LOCAL));
        stateManager.patchState(Map.of(), Scope.LOCAL);
        assertEquals(0, stateManager.replaceCount);
        assertTrue(stateManager.getState(Scope.LOCAL).getStateVersion().isEmpty());

        final Map<String, String> firstPatch = new HashMap<>();
        firstPatch.put("a", "1");
        firstPatch.put("b", null);
        firstPatch.put("c", "");
        stateManager.patchState(firstPatch, Scope.LOCAL);
        final StateMap firstState = stateManager.getState(Scope.LOCAL);
        assertEquals(Map.of("a", "1", "c", ""), firstState.toMap());
        assertTrue(firstState.getStateVersion().isPresent());

        final Map<String, String> secondPatch = new HashMap<>();
        secondPatch.put("a", "2");
        secondPatch.put("missing", null);
        stateManager.patchState(secondPatch, Scope.LOCAL);
        assertEquals(Map.of("a", "2", "c", ""), stateManager.getState(Scope.LOCAL).toMap());

        final Map<String, String> thirdPatch = new HashMap<>();
        thirdPatch.put("c", null);
        stateManager.patchState(thirdPatch, Scope.LOCAL);
        assertEquals(Map.of("a", "2"), stateManager.getState(Scope.LOCAL).toMap());
        assertEquals(3, stateManager.replaceCount);
    }

    @Test
    public void testPatchStateRetriesAfterFailedReplace() throws Exception {
        final InMemoryStateManager stateManager = new InMemoryStateManager();
        stateManager.setState(Map.of("a", "1"), Scope.LOCAL);
        stateManager.writeBeforeNextReplace = Map.of("x", "1");

        stateManager.patchState(Map.of("b", "2"), Scope.LOCAL);

        assertEquals(Map.of("a", "1", "b", "2", "x", "1"), stateManager.getState(Scope.LOCAL).toMap());
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
