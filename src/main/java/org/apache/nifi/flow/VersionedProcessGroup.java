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

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashSet;
import java.util.Set;

public class VersionedProcessGroup extends VersionedComponent {

    private Set<VersionedProcessGroup> processGroups = new HashSet<>();
    private Set<VersionedRemoteProcessGroup> remoteProcessGroups = new HashSet<>();
    private Set<VersionedProcessor> processors = new HashSet<>();
    private Set<VersionedPort> inputPorts = new HashSet<>();
    private Set<VersionedPort> outputPorts = new HashSet<>();
    private Set<VersionedConnection> connections = new HashSet<>();
    private Set<VersionedLabel> labels = new HashSet<>();
    private Set<VersionedFunnel> funnels = new HashSet<>();
    private Set<VersionedControllerService> controllerServices = new HashSet<>();
    private VersionedFlowCoordinates versionedFlowCoordinates = null;

    private String parameterContextName;
    private String flowfileConcurrency;
    private String flowfileOutboundPolicy;

    private String defaultFlowFileExpiration;
    private Long defaultBackPressureObjectThreshold;
    private String defaultBackPressureDataSizeThreshold;
    private ScheduledState scheduledState;
    private ExecutionEngine executionEngine;
    private Integer maxConcurrentTasks;
    private String statelessFlowTimeout;

    private String logFileSuffix;


    @Schema(description = "The child Process Groups")
    public Set<VersionedProcessGroup> getProcessGroups() {
        return processGroups;
    }

    public void setProcessGroups(Set<VersionedProcessGroup> processGroups) {
        this.processGroups = new HashSet<>(processGroups);
    }

    @Schema(description = "The Remote Process Groups")
    public Set<VersionedRemoteProcessGroup> getRemoteProcessGroups() {
        return remoteProcessGroups;
    }

    public void setRemoteProcessGroups(Set<VersionedRemoteProcessGroup> remoteProcessGroups) {
        this.remoteProcessGroups = new HashSet<>(remoteProcessGroups);
    }

    @Schema(description = "The Processors")
    public Set<VersionedProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(Set<VersionedProcessor> processors) {
        this.processors = new HashSet<>(processors);
    }

    @Schema(description = "The Input Ports")
    public Set<VersionedPort> getInputPorts() {
        return inputPorts;
    }

    public void setInputPorts(Set<VersionedPort> inputPorts) {
        this.inputPorts = new HashSet<>(inputPorts);
    }

    @Schema(description = "The Output Ports")
    public Set<VersionedPort> getOutputPorts() {
        return outputPorts;
    }

    public void setOutputPorts(Set<VersionedPort> outputPorts) {
        this.outputPorts = new HashSet<>(outputPorts);
    }

    @Schema(description = "The Connections")
    public Set<VersionedConnection> getConnections() {
        return connections;
    }

    public void setConnections(Set<VersionedConnection> connections) {
        this.connections = new HashSet<>(connections);
    }

    @Schema(description = "The Labels")
    public Set<VersionedLabel> getLabels() {
        return labels;
    }

    public void setLabels(Set<VersionedLabel> labels) {
        this.labels = new HashSet<>(labels);
    }

    @Schema(description = "The Funnels")
    public Set<VersionedFunnel> getFunnels() {
        return funnels;
    }

    public void setFunnels(Set<VersionedFunnel> funnels) {
        this.funnels = new HashSet<>(funnels);
    }

    @Schema(description = "The Controller Services")
    public Set<VersionedControllerService> getControllerServices() {
        return controllerServices;
    }

    public void setControllerServices(Set<VersionedControllerService> controllerServices) {
        this.controllerServices = new HashSet<>(controllerServices);
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.PROCESS_GROUP;
    }

    public void setVersionedFlowCoordinates(VersionedFlowCoordinates flowCoordinates) {
        this.versionedFlowCoordinates = flowCoordinates;
    }

    @Schema(description = "The coordinates where the remote flow is stored, or null if the Process Group is not directly under Version Control")
    public VersionedFlowCoordinates getVersionedFlowCoordinates() {
        return versionedFlowCoordinates;
    }

    @Schema(description = "The name of the parameter context used by this process group")
    public String getParameterContextName() {
        return parameterContextName;
    }

    public void setParameterContextName(String parameterContextName) {
        this.parameterContextName = parameterContextName;
    }

    @Schema(description = "The configured FlowFile Concurrency for the Process Group")
    public String getFlowFileConcurrency() {
        return flowfileConcurrency;
    }

    public void setFlowFileConcurrency(final String flowfileConcurrency) {
        this.flowfileConcurrency = flowfileConcurrency;
    }

    @Schema(description = "The FlowFile Outbound Policy for the Process Group")
    public String getFlowFileOutboundPolicy() {
        return flowfileOutboundPolicy;
    }

    public void setFlowFileOutboundPolicy(final String outboundPolicy) {
        this.flowfileOutboundPolicy = outboundPolicy;
    }

    @Schema(description = "The default FlowFile Expiration for this Process Group.")
    public String getDefaultFlowFileExpiration() {
        return defaultFlowFileExpiration;
    }

    public void setDefaultFlowFileExpiration(String defaultFlowFileExpiration) {
        this.defaultFlowFileExpiration = defaultFlowFileExpiration;
    }

    @Schema(description = "Default value used in this Process Group for the maximum number of objects that can be queued before back pressure is applied.")
    public Long getDefaultBackPressureObjectThreshold() {
        return defaultBackPressureObjectThreshold;
    }

    public void setDefaultBackPressureObjectThreshold(final Long defaultBackPressureObjectThreshold) {
        this.defaultBackPressureObjectThreshold = defaultBackPressureObjectThreshold;
    }

    @Schema(description = "Default value used in this Process Group for the maximum data size of objects that can be queued before back pressure is applied.")
    public String getDefaultBackPressureDataSizeThreshold() {
        return defaultBackPressureDataSizeThreshold;
    }

    public void setDefaultBackPressureDataSizeThreshold(final String defaultBackPressureDataSizeThreshold) {
        this.defaultBackPressureDataSizeThreshold = defaultBackPressureDataSizeThreshold;
    }

    @Schema(description = "The log file suffix for this Process Group for dedicated logging.")
    public String getLogFileSuffix() {
        return logFileSuffix;
    }

    public void setLogFileSuffix(final String logFileSuffix) {
        this.logFileSuffix = logFileSuffix;
    }

    @Schema(description = "The Scheduled State of the Process Group, if the group is configured to use the Stateless Execution Engine. Otherwise, this value has no relevance.")
    public ScheduledState getScheduledState() {
        return scheduledState;
    }

    public void setScheduledState(final ScheduledState scheduledState) {
        this.scheduledState = scheduledState;
    }

    @Schema(description = "The Execution Engine that should be used to run the components within the group.")
    public ExecutionEngine getExecutionEngine() {
        return executionEngine;
    }

    public void setExecutionEngine(final ExecutionEngine executionEngine) {
        this.executionEngine = executionEngine;
    }

    @Schema(description = "The maximum number of concurrent tasks that should be scheduled for this Process Group when using the Stateless Engine")
    public Integer getMaxConcurrentTasks() {
        return maxConcurrentTasks;
    }

    public void setMaxConcurrentTasks(final Integer maxConcurrentTasks) {
        this.maxConcurrentTasks = maxConcurrentTasks;
    }

    @Schema(description = "The maximum amount of time that the flow is allows to run using the Stateless engine before it times out and is considered a failure")
    public String getStatelessFlowTimeout() {
        return statelessFlowTimeout;
    }

    public void setStatelessFlowTimeout(final String timeout) {
        this.statelessFlowTimeout = timeout;
    }
}
