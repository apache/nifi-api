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

import org.apache.nifi.controller.queue.QueueSize;
import org.apache.nifi.flowfile.FlowFile;

import java.util.List;

/**
 * <p>
 *     A point-in-time view of a connection's queue, returned by
 *     {@link ConnectionFacade#getQueueSnapshot()}. Bundles the connection's total {@link QueueSize}
 *     together with the {@link FlowFile}s currently held in this node's active in-memory queue,
 *     meaning the FlowFiles resident in memory on this node and available in poll order.
 * </p>
 *
 * <p>
 *     The active list and the {@link QueueSize} are captured atomically, so they describe the same
 *     point in time. The active list is not always the entire queue. FlowFiles that have been
 *     swapped out are excluded from {@link #getActiveFlowFiles()}. On load-balanced connections,
 *     FlowFiles in flight between nodes are excluded as well. Those FlowFiles are still counted in
 *     {@link #getQueueSize()}. Use {@link #isActiveListExhaustive()} to determine whether the
 *     active list contains every FlowFile counted in the queue size.
 * </p>
 *
 * <p>
 *     {@link #getActiveFlowFiles()} returns the full active list without a caller-supplied limit, but
 *     the list itself is bounded by the framework's maximum active in-memory queue size. The
 *     snapshot does not clone the {@link FlowFile} instances; it references the existing active
 *     FlowFiles.
 * </p>
 */
public interface QueueSnapshot {

    /**
     * @return the total {@link QueueSize} of the connection at the time the snapshot was taken
     */
    QueueSize getQueueSize();

    /**
     * @return the active in-memory FlowFiles in poll order, never null; excludes FlowFiles that
     *         have been swapped out and, for load-balanced connections, FlowFiles in flight between
     *         nodes
     */
    List<FlowFile> getActiveFlowFiles();

    /**
     * @return {@code true} if {@link #getActiveFlowFiles()} contains every FlowFile counted in
     *         {@link #getQueueSize()}; otherwise {@code false}
     */
    boolean isActiveListExhaustive();
}
