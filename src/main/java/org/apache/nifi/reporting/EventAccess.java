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
package org.apache.nifi.reporting;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.nifi.action.Action;
import org.apache.nifi.controller.status.ProcessGroupStatus;
import org.apache.nifi.diagnostics.StorageUsage;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventRepository;

public interface EventAccess {

    /**
     * @return the status for all components in this Controller
     */
    ProcessGroupStatus getControllerStatus();

    /**
     * @param groupId Process Group Identifier
     * @return the status of all components in the specified group.
     */
    ProcessGroupStatus getGroupStatus(final String groupId);

    /**
     * Convenience method to obtain Provenance Events starting with (and
     * including) the given ID. If no event exists with that ID, the first event
     * to be returned will be have an ID greater than <code>firstEventId</code>.
     *
     * @param firstEventId the ID of the first event to obtain
     * @param maxRecords the maximum number of records to obtain
     * @return event records matching query
     * @throws java.io.IOException if unable to get records
     */
    List<ProvenanceEventRecord> getProvenanceEvents(long firstEventId, final int maxRecords) throws IOException;

    /**
     * @return the Provenance Event Repository
     */
    ProvenanceEventRepository getProvenanceRepository();

    /**
     * Obtains flow changes starting with (and including) the given action ID. If no action
     * exists with that ID, the first action to be returned will have an ID greater than
     * <code>firstActionId</code>.
     *
     * @param firstActionId the id of the first action to obtain
     * @param maxActions the maximum number of actions to obtain
     * @return actions with ids greater than or equal to firstActionID, up to the max number of actions
     */
    List<Action> getFlowChanges(int firstActionId, final int maxActions);

    /**
     * Returns the total number of bytes read by this instance (at the root process group level, i.e. all events) since the instance started
     *
     * @return the total number of bytes read by this instance
     */
    long getTotalBytesRead();

    /**
     * Returns the total number of bytes written by this instance (at the root process group level, i.e. all events) since the instance started
     *
     * @return the total number of bytes written by this instance
     */
    long getTotalBytesWritten();

    /**
     * Returns the total number of bytes sent by this instance (at the root process group level) since the instance started
     *
     * @return the total number of bytes sent by this instance
     */
    long getTotalBytesSent();

    /**
     * Returns the total number of bytes received by this instance (at the root process group level) since the instance started
     *
     * @return the total number of bytes received by this instance
     */
    long getTotalBytesReceived();

    /**
     * Returns the storage usage of all provenance repositories
     * @return the map of all the storage usage
     */
    Map<String, StorageUsage> getProvenanceRepositoryStorageUsage();

    /**
     * Returns the storage usage of all content repositories
     * @return the map of all the storage usage
     */
    Map<String, StorageUsage> getContentRepositoryStorageUsage();

    /**
     * Returns the storage usage of the flow file repository
     * @return the storage usage
     */
    StorageUsage getFlowFileRepositoryStorageUsage();

}
