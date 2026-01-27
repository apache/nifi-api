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

package org.apache.nifi.components.connector;

import org.apache.nifi.flow.Bundle;

import java.util.List;
import java.util.Optional;

public interface ComponentBundleLookup {

    /**
     * Determines which bundles are available for the given component type.
     * @param componentType the component type
     * @return the list of available bundles that contain the component type
     */
    List<Bundle> getAvailableBundles(String componentType);

    /**
     * Determines the latest bundle available for the given component type based on version comparison.
     *
     * <p>Version comparison follows these rules:</p>
     * <ol>
     *   <li>Versions are split by dots (e.g., "2.1.0" becomes ["2", "1", "0"]).
     *       This also supports calendar-date formats (e.g., "2026.01.01").</li>
     *   <li>Each part is compared numerically when possible; numeric parts are considered
     *       newer than non-numeric parts (e.g., "2.0.0" &gt; "2.0.next")</li>
     *   <li>When base versions are equal, qualifiers are compared with the following precedence
     *       (highest to lowest):
     *       <ul>
     *         <li>Release (no qualifier)</li>
     *         <li>RC (Release Candidate) - e.g., "-RC1", "-RC2"</li>
     *         <li>M (Milestone) - e.g., "-M1", "-M2"</li>
     *         <li>Other/unknown qualifiers</li>
     *         <li>SNAPSHOT</li>
     *       </ul>
     *   </li>
     *   <li>Within the same qualifier type, numeric suffixes are compared
     *       (e.g., "2.0.0-RC2" &gt; "2.0.0-RC1", "2.0.0-M4" &gt; "2.0.0-M1")</li>
     * </ol>
     *
     * <p>Examples of version ordering (highest to lowest):</p>
     * <ul>
     *   <li>3.0.0 &gt; 2.1.0 &gt; 2.0.0 &gt; 2.0.0-RC2 &gt; 2.0.0-RC1 &gt; 2.0.0-M4 &gt; 2.0.0-M1 &gt; 2.0.0-SNAPSHOT</li>
     *   <li>2.1.0-SNAPSHOT &gt; 2.0.0 (higher base version wins)</li>
     *   <li>2026.01.01 &gt; 2025.12.31 (calendar-date format)</li>
     * </ul>
     *
     * @param componentType the component type
     * @return an Optional containing the latest available bundle, or empty if no bundles are available
     */
    Optional<Bundle> getLatestBundle(String componentType);

}
