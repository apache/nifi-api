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
import org.apache.nifi.flow.VersionedProcessGroup;
import org.apache.nifi.flowfile.FlowFile;

import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;

public interface ProcessGroupFacade {

    VersionedProcessGroup getDefinition();

    ProcessorFacade getProcessor(String id);

    Set<ProcessorFacade> getProcessors();

    ControllerServiceFacade getControllerService(String id);

    Set<ControllerServiceFacade> getControllerServices();

    Set<ControllerServiceFacade> getControllerServices(ControllerServiceReferenceScope referenceScope, ComponentHierarchyScope hierarchyScope);

    ConnectionFacade getConnection(String id);

    Set<ConnectionFacade> getConnections();

    ProcessGroupFacade getProcessGroup(String id);

    Set<ProcessGroupFacade> getProcessGroups();

    QueueSize getQueueSize();

    boolean isFlowEmpty();

    StatelessGroupLifecycle getStatelessLifecycle();

    ProcessGroupLifecycle getLifecycle();

    /**
     * Drops all FlowFiles from all connections in this ProcessGroup and its child ProcessGroups
     * that match the given predicate.
     *
     * @param predicate the predicate to test each FlowFile against
     * @return a summary of the dropped FlowFiles
     * @throws IOException if an I/O error occurs while dropping FlowFiles
     */
    DropFlowFileSummary dropFlowFiles(Predicate<FlowFile> predicate) throws IOException;

}
