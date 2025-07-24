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

    CompletableFuture<Void> enableControllerServices(ControllerServiceReferenceScope scope, ControllerServiceReferenceHierarchy hierarchy);

    CompletableFuture<Void> enableControllerServices(Collection<String> serviceIdentifiers);

    CompletableFuture<Void> disableControllerServices(ControllerServiceReferenceHierarchy hierarchy);

    CompletableFuture<Void> disableControllerServices(Collection<String> serviceIdentifiers);

    CompletableFuture<Void> startProcessors(boolean recursive);

    CompletableFuture<Void> start(ControllerServiceReferenceScope serviceReferenceScope);

    CompletableFuture<Void> stop();

    CompletableFuture<Void> stopProcessors(boolean recursive);

    CompletableFuture<Void> startPorts(boolean recursive);

    CompletableFuture<Void> stopPorts(boolean recursive);

    CompletableFuture<Void> startRemoteProcessGroups(boolean recursive);

    CompletableFuture<Void> stopRemoteProcessGroups(boolean recursive);

    CompletableFuture<Void> startStatelessGroups(boolean recursive);

    CompletableFuture<Void> stopStatelessGroups(boolean recursive);

    int getActiveThreadCount();
}
