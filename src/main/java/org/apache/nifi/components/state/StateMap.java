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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides a representation of a component's state at some point in time.
 */
public interface StateMap {

    /**
     * Get state version is not guaranteed to be numeric, but can be used to compare against an expected version.
     * The default implementation uses the available version number and considers -1 as indicating an empty version
     *
     * @return State version or empty when not known
     */
    Optional<String> getStateVersion();

    /**
     * Returns the value associated with the given key
     *n
     * @param key the key whose value should be retrieved
     * @return the value associated with the given key, or <code>null</code> if no value is associated
     *         with this key.
     */
    String get(String key);

    /**
     * Returns an immutable Map representation of all keys and values for the state of a component.
     *
     * @return an immutable Map representation of all keys and values for the state of a component.
     */
    Map<String, String> toMap();

    /**
     * Returns an immutable Map of all keys and values for the state of a component, with the given patches applied. This StateMap is not changed.
     * <p>
     * Each key in the given patches with a non-null value is added or overwritten. Each key with a {@code null} value is removed.
     * Keys that are not in the given patches keep their values from this StateMap.
     * </p>
     *
     * @param patches a map of state keys to their new values. A key that is mapped to {@code null} is removed from the result. The map itself must not be {@code null}.
     * @return an immutable Map of this state's keys and values with the given patches applied
     */
    default Map<String, String> toMap(final Map<String, String> patches) {
        Objects.requireNonNull(patches, "Patches required");
        if (patches.isEmpty()) {
            return toMap();
        }

        final Map<String, String> patchedValues = new HashMap<>(toMap());
        for (final Map.Entry<String, String> entry : patches.entrySet()) {
            if (entry.getValue() == null) {
                patchedValues.remove(entry.getKey());
            } else {
                patchedValues.put(entry.getKey(), entry.getValue());
            }
        }

        return Collections.unmodifiableMap(patchedValues);
    }
}
