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

    /**
     * Begins draining the Process Group. While the drain is active, the components that bring data into the group remain stopped and the
     * rest of the group runs in order to process the data that is already queued. The drain does not end on its own, even once no data
     * remains; it stays active until {@link #endDrain()} is called.
     *
     * @return a future that completes once the components that bring data into the group have stopped, the Controller Services referenced
     *         by the group's components have been enabled, the other components in the group have been told to start, and the methods annotated
     *         with {@link org.apache.nifi.annotation.lifecycle.OnDrainInitiated OnDrainInitiated} have been scheduled to be called. The future does
     *         not wait for those methods to return. If any of these steps fails, the future completes exceptionally, the drain is no longer active,
     *         and any components that were already stopped or started are left as they are.
     * @throws IllegalStateException if this group, a parent group, or a child group is already being drained, if this group uses the
     *         Stateless Execution Engine, or if a component that brings data into the group cannot be stopped
     */
    default CompletableFuture<Void> beginDrain() {
        throw new UnsupportedOperationException("Draining is not supported by " + getClass().getName());
    }

    /**
     * Ends the active drain of the Process Group. The caller decides when a drain is over, either because all data has been processed or
     * because the drain has been cancelled. Once the drain has ended, a new drain may begin. For each component whose methods annotated with
     * {@link org.apache.nifi.annotation.lifecycle.OnDrainInitiated OnDrainInitiated} were scheduled to be called for this drain, the methods
     * annotated with {@link org.apache.nifi.annotation.lifecycle.OnDrainCompleted OnDrainCompleted} are called once its
     * {@link org.apache.nifi.annotation.lifecycle.OnDrainInitiated OnDrainInitiated} methods have returned. This method does not wait for them.
     * Ending a drain does not start or stop any components: the components that bring data into the group remain stopped and the rest of the
     * group keeps running. If the drain is ended before the future returned by {@link #beginDrain()} completes, components that have not yet
     * been told to start are not started, and the future still completes normally. Calling this method when no drain is active has no effect.
     */
    default void endDrain() {
    }
}
