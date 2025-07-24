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
    private ScheduledState scheduledState;
    private List<VersionedConfigurationStep> activeFlowConfiguration;
    private List<VersionedConfigurationStep> workingFlowConfiguration;
    private String type;
    private Bundle bundle;

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

    public ScheduledState getScheduledState() {
        return scheduledState;
    }

    public void setScheduledState(final ScheduledState scheduledState) {
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
}
