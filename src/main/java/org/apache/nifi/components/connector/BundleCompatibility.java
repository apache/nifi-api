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

/**
 * Specifies how bundle resolution should be handled when updating a flow with components
 * whose specified bundles may not be available in the NiFi instance.
 */
public enum BundleCompatibility {

    /**
     * Requires the exact bundle specified in the flow. If the bundle is not available,
     * a ghosted component will be created. This is the default behavior.
     */
    REQUIRE_EXACT_BUNDLE,

    /**
     * If the specified bundle is not available, checks what bundles are available for
     * the given component type. If exactly one bundle is available, resolves to that bundle.
     * If no bundles or multiple bundles are available, uses the specified bundle and
     * creates a ghosted component.
     */
    RESOLVE_BUNDLE,

    /**
     * If the specified bundle is not available, checks what bundles are available for
     * the given component type. If no bundles are available, uses the specified bundle
     * and creates a ghosted component. Otherwise, uses the newest available bundle
     * based on version comparison.
     */
    RESOLVE_NEWEST_BUNDLE
}
