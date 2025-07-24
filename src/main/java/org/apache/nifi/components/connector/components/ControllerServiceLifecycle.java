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

package org.apache.nifi.components.connector.components;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ControllerServiceLifecycle {

    ControllerServiceState getState();

    CompletableFuture<Void> enable();

    /**
     * Enables the Controller Service using the provided property value overrides. The overrides are merged with
     * the currently configured property values, with the overrides taking precedence. This allows enabling a service
     * with temporary property value overrides without modifying the service's configuration.
     *
     * @param propertyValueOverrides the property value overrides to apply when enabling the service
     * @return a CompletableFuture that completes when the service has been enabled
     */
    CompletableFuture<Void> enable(Map<String, String> propertyValueOverrides);

    CompletableFuture<Void> disable();

}
