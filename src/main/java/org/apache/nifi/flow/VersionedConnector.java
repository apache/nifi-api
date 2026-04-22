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

package org.apache.nifi.flow;

import java.util.List;

public class VersionedConnector {
    private String instanceIdentifier;
    private String name;
    private VersionedConnectorState scheduledState;
    private List<VersionedConfigurationStep> activeFlowConfiguration;
    private List<VersionedConfigurationStep> workingFlowConfiguration;
    private String type;
    private Bundle bundle;
    private VersionedProcessGroup managedProcessGroup;

    public String getInstanceIdentifier() {
        return instanceIdentifier;
    }

    public void setInstanceIdentifier(final String instanceIdentifier) {
        this.instanceIdentifier = instanceIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public VersionedConnectorState getScheduledState() {
        return scheduledState;
    }

    public void setScheduledState(final VersionedConnectorState scheduledState) {
        this.scheduledState = scheduledState;
    }

    public List<VersionedConfigurationStep> getActiveFlowConfiguration() {
        return activeFlowConfiguration;
    }

    public void setActiveFlowConfiguration(final List<VersionedConfigurationStep> configurationSteps) {
        this.activeFlowConfiguration = configurationSteps;
    }

    public List<VersionedConfigurationStep> getWorkingFlowConfiguration() {
        return workingFlowConfiguration;
    }

    public void setWorkingFlowConfiguration(final List<VersionedConfigurationStep> workingFlowConfiguration) {
        this.workingFlowConfiguration = workingFlowConfiguration;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(final Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Returns the contents of the Connector's Managed Process Group. This field is populated only when the Connector is
     * in Troubleshooting mode at the time the flow is persisted, so that any user modifications made while in Troubleshooting
     * survive a restart. For any other Connector state, this field is {@code null} and the Managed Process Group's contents
     * are reconstructed from the configuration steps upon restore.
     *
     * @return the persisted contents of the Managed Process Group, or {@code null} if the Connector was not in Troubleshooting
     */
    public VersionedProcessGroup getManagedProcessGroup() {
        return managedProcessGroup;
    }

    public void setManagedProcessGroup(final VersionedProcessGroup managedProcessGroup) {
        this.managedProcessGroup = managedProcessGroup;
    }
}
