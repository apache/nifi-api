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

import org.apache.nifi.components.connector.DropFlowFileSummary;
import org.apache.nifi.controller.queue.QueueSize;
import org.apache.nifi.flow.VersionedConnection;
import org.apache.nifi.flowfile.FlowFile;

import java.io.IOException;
import java.util.function.Predicate;

public interface ConnectionFacade {

    VersionedConnection getDefinition();

    /**
     * Returns the size of the Connection's queue.
     * @return the size of the Connection's queue
     */
    QueueSize getQueueSize();

    /**
     * Purges all data from the connection.
     */
    void purge();

    /**
     * Drops all FlowFiles from the connection that match the given predicate.
     *
     * @param predicate the predicate to use to determine which FlowFiles to drop
     * @return a summary of the FlowFiles that were dropped
     * @throws IOException if an I/O error occurs while dropping FlowFiles
     */
    DropFlowFileSummary dropFlowFiles(Predicate<FlowFile> predicate) throws IOException;

}
