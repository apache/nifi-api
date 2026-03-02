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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ProcessGroupLifecycle {

    CompletableFuture<Void> enableControllerServices(ControllerServiceReferenceScope referenceScope, ComponentHierarchyScope hierarchyScope);

    CompletableFuture<Void> enableControllerServices(Collection<String> serviceIdentifiers);

    CompletableFuture<Void> disableControllerServices(ComponentHierarchyScope scope);

    CompletableFuture<Void> disableControllerServices(Collection<String> serviceIdentifiers);

    CompletableFuture<Void> startProcessors(ComponentHierarchyScope scope);

    CompletableFuture<Void> start(ControllerServiceReferenceScope serviceReferenceScope);

    CompletableFuture<Void> stop();

    CompletableFuture<Void> stopProcessors(ComponentHierarchyScope scope);

    CompletableFuture<Void> startPorts(ComponentHierarchyScope scope);

    CompletableFuture<Void> stopPorts(ComponentHierarchyScope scope);

    CompletableFuture<Void> startRemoteProcessGroups(ComponentHierarchyScope scope);

    CompletableFuture<Void> stopRemoteProcessGroups(ComponentHierarchyScope scope);

    CompletableFuture<Void> startStatelessGroups(ComponentHierarchyScope scope);

    CompletableFuture<Void> stopStatelessGroups(ComponentHierarchyScope scope);

    int getActiveThreadCount();
}
