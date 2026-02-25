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

package org.apache.nifi.components.connector;

import org.apache.nifi.components.ConfigVerificationResult;
import org.apache.nifi.components.DescribedValue;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.flow.VersionedExternalFlow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *     A Connector is a component that encapsulates and manages a NiFi flow, in such a way that the flow
 *     can be treated as a single component. The Connector is responsible for managing the lifecycle of the flow,
 *     including starting and stopping the flow, as well as validating that the flow is correctly configured.
 *     The Connector exposes a single holistic configuration that is encapsulates the configuration of the
 *     sources, sinks, transformations, routing logic, and any other components that make up the flow.
 * </p>
 *
 * <p>
 *     Importantly, a Connector represents a higher-level abstraction and is capable of manipulating the associated
 *     dataflow, including adding, removing, and configuring components within the flow. This allows a single entity to
 *     be provided such that configuring properties can result in a flow being dynamically reconfigured (e.g., using a
 *     different Controller Service implementation).
 * </p>
 *
 * <p>
 *     When a flow definition is created and shared in NiFi, it can be easily instantiated in another NiFi instance.
 *     The new instance can then be configured via Parameters. However, if more complex configuration is required, such as
 *     choosing which Controller Service to use, or enabling or disabling a particular transformation step, the user must
 *     understand how to manipulate the flow directly. Connectors provide the ability to encapsulate such complexity behind
 *     a higher-level abstraction, allowing users to configure the flow via a guided configuration experience.
 * </p>
 *
 * <p>
 *     The Connector API makes use of a {@link FlowContext} abstraction in order to provide effectively two separate instances
 *     of a flow: the Active flow that can be stopped and started in order to process and move data, and a Working flow that
 *     can be used to verify configuration in order to ensure that when the configuration is applied to the Active flow, it will
 *     function as desired.
 * </p>
 *
 * <b>Implementation Note:</b> This API is currently experimental, as it is under very active development. As such,
 * it is subject to change without notice between minor releases.
 */
public interface Connector {

    /**
     * Initializes the Connector instance, providing it the necessary context that it needs to operate.
     * @param context the context for initialization
     */
    void initialize(ConnectorInitializationContext context);

    /**
     * Provides the initial version of the flow that this Connector manages. The Active Flow Context will be
     * updated to reflect this flow when the Connector is first added to the NiFi instance but not when the Connector
     * is reinitialized upon restart of NiFi.
     *
     * @return the initial version of the flow
     */
    VersionedExternalFlow getInitialFlow();

    /**
     * Starts the Connector instance.
     * @throws FlowUpdateException if there is an error starting the Connector
     * @param activeFlowContext the active flow context
     */
    void start(FlowContext activeFlowContext) throws FlowUpdateException;

    /**
     * Stops the Connector instance.
     * @throws FlowUpdateException if there is an error stopping the Connector
     * @param activeFlowContext the active flow context
     */
    void stop(FlowContext activeFlowContext) throws FlowUpdateException;

    /**
     * Validates that the Connector is valid according to its current configuration. Validity of a Connector may be
     * defined simply as the all components being valid, or it may encompass more complex validation logic, such
     * as ensuring that a Source Processor is able to connect to a remote system, or that a Sink Processor
     * is able to write to a remote system.
     *
     * @param activeFlowContext the active flow context
     * @param validationContext the context for validation
     *
     * @return a list of ValidationResults, each of which may indicate a check that was performed and any associated explanations
     * as to why the Connector is valid or invalid.
     */
    List<ValidationResult> validate(FlowContext activeFlowContext, ConnectorValidationContext validationContext);

    /**
     * Validates the configuration for a specific configuration step. This allows the Connector to indicate any
     * issues with syntactic configuration issues but is not as comprehensive as the overall validation provided
     * by {@link #validate(FlowContext, ConnectorValidationContext)} due to the fact that it does not have access
     * to the full configuration of the Connector. This provides immediate feedback to users
     * as they are configuring each step.
     *
     * @param configurationStep the configuration step being validated
     * @param configurationContext the context for the configuration
     * @param validationContext the context for validation
     * @return a list of ValidationResults, each of which may indicate a check that was performed and any associated explanations
     * as to why the configuration step is valid or invalid.
     */
    List<ValidationResult> validateConfigurationStep(ConfigurationStep configurationStep, ConnectorConfigurationContext configurationContext, ConnectorValidationContext validationContext);

    /**
     * Verifies the configuration for a specific configuration step. This allows the Connector to perform
     * more comprehensive verification of the configuration for a step than does validation, such as attempting to connect to
     * remote systems, sample data and ensure that it can be parsed correctly, etc.
     *
     * @param stepName the name of the configuration step being verified
     * @param propertyValueOverrides any overrides to the currently configured property values that should be used for verification
     * @param flowContext the flow context that is being used for the verification
     * @return a list of ConfigVerificationResults, each of which may indicate a check that was performed and any associated explanation
     * as to why the configuration step verification succeeded, failed, or was skipped.
     */
    List<ConfigVerificationResult> verifyConfigurationStep(String stepName, Map<String, String> propertyValueOverrides, FlowContext flowContext);

    /**
     * Verifies the overall configuration of the Connector based on the configuration that has already been provided for the given Flow Context.
     *
     * @param flowContext the flow context that houses the configuration being used to drive the verification
     * @return a list of ConfigVerificationResults, each of which may indicate a check that was performed and any associated explanation
     * as to why the configuration verification succeeded, failed, or was skipped.
     */
    List<ConfigVerificationResult> verify(FlowContext flowContext);

    /**
     * Returns the list of configuration steps that define the configuration of this Connector. Each step
     * represents a logical grouping of properties that should be configured together. The order of the steps
     * in the list represents the order in which the steps should be configured.
     *
     * @return the list of configuration steps
     */
    List<ConfigurationStep> getConfigurationSteps();

    /**
     * Called whenever a specific configuration step has been configured. This allows the Connector to perform any necessary
     * actions specific to that step, such as updating parameter values, updating the flow, etc.
     *
     * @param stepName the name of the step
     * @param workingFlowContext the working flow context that is being used for the update
     */
    void onConfigurationStepConfigured(String stepName, FlowContext workingFlowContext) throws FlowUpdateException;

    /**
     * Called before any updates to the Connector's configuration are applied. This allows the Connector to perform any necessary
     * preparation work before the configuration is changed, such as stopping the flow, draining queues, etc.
     *
     * @param workingFlowContext the working flow context that has been created for the update
     * @param activeFlowContext the active flow context that is currently in use
     */
    void prepareForUpdate(FlowContext workingFlowContext, FlowContext activeFlowContext) throws FlowUpdateException;

    /**
     * Called if the update preparation (i.e., {@link #prepareForUpdate(FlowContext, FlowContext)}) fails or is cancelled.
     * This allows the Connector to perform any necessary
     * cleanup work after a failed preparation, such as cancelling any in-progress operations, etc.
     *
     * @param workingFlowContext the working flow context that was being used for the update preparation
     * @param cause the cause for the update preparation to be aborted
     */
    void abortUpdate(FlowContext workingFlowContext, Throwable cause);

    /**
     * Applies the configuration of the working FlowContext to the active flow. Once the active FlowContext has been updated,
     * the existing working FlowContext is destroyed, along with any components that are part of the flow and any FlowFiles that
     * might be queued up as part of the flow. A new working FlowContext is then created that reflects the newly updated active flow.
     *
     * @param workingFlowContext the working flow context that represents the updated configuration
     * @param activeFlowContext the flow context that represents the active flow
     */
    void applyUpdate(FlowContext workingFlowContext, FlowContext activeFlowContext) throws FlowUpdateException;

    /**
     * Fetches the values that are allowed to be configured for a given property.
     *
     * @param stepName the name of the {@link ConfigurationStep}
     * @param propertyName the name of the {@link ConnectorPropertyDescriptor} within the given ConfigurationStep
     * @param flowContext the FlowContext pertinent to the request
     * @return the list of values that are allowed to be configured for the given property.
     */
    List<DescribedValue> fetchAllowableValues(String stepName, String propertyName, FlowContext flowContext);

    /**
     * Fetches only the values that are allowed to be configured for a given property that contain the text of
     * the given filter.
     *
     * @param stepName the name of the {@link ConfigurationStep}
     * @param propertyName the name of the {@link ConnectorPropertyDescriptor} within the given ConfigurationStep
     * @param flowContext the FlowContext pertinent to the request
     * @param filter the text that should be contained within the values that are returned
     * @return the list of values that are allowed to be configured for the given property.
     */
    List<DescribedValue> fetchAllowableValues(String stepName, String propertyName, FlowContext flowContext, String filter);

    /**
     * Drains any in-flight FlowFiles from the flow associated with the given Flow Context by processing the existing data
     * but not accepting any new data.
     * @param flowContext the flow context
     * @return a Future that will be completed when the draining is complete
     */
    CompletableFuture<Void> drainFlowFiles(FlowContext flowContext);
}
