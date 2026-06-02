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
import org.apache.nifi.components.connector.components.ConnectionFacade;
import org.apache.nifi.components.connector.components.ControllerServiceFacade;
import org.apache.nifi.components.connector.components.ComponentHierarchyScope;
import org.apache.nifi.components.connector.components.ControllerServiceReferenceScope;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.components.connector.components.ProcessGroupFacade;
import org.apache.nifi.components.connector.components.ProcessGroupLifecycle;
import org.apache.nifi.components.connector.components.ProcessorFacade;
import org.apache.nifi.components.connector.components.ProcessorState;
import org.apache.nifi.controller.queue.QueueSize;
import org.apache.nifi.flow.VersionedConnection;
import org.apache.nifi.logging.ComponentLog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractConnector implements Connector {
    private volatile ConnectorInitializationContext initializationContext;
    private volatile ComponentLog logger;
    private volatile CompletableFuture<Void> prepareUpdateFuture;
    private String description; // effectively final

    /**
     * Called whenever a specific configuration step has been configured. This allows the Connector to perform any necessary
     * actions in response to the configuration step being configured. A typical pattern is to update the flow so that in subsequent
     * configuration steps, the properties of the step are available for use when verifying configuration or fetching allowable values.
     *
     * @param stepName the name of the configuration step that has been configured
     * @param workingContext the working flow context that is being used for the configuration step
     * @throws FlowUpdateException if there is an error performing the necessary actions in response to the configuration step being configured
     */
    protected abstract void onStepConfigured(final String stepName, final FlowContext workingContext) throws FlowUpdateException;


    @Override
    public final void initialize(final ConnectorInitializationContext context) {
        this.initializationContext = context;
        this.logger = context.getLogger();
        this.description = getClass().getSimpleName() + "[id=" + context.getIdentifier() + "]";

        init();
    }

    /**
     * No-op method for subclasses to override to perform any initialization logic
     */
    protected void init() {
    }

    protected final ComponentLog getLogger() {
        return logger;
    }

    protected final ConnectorInitializationContext getInitializationContext() {
        if (initializationContext == null) {
            throw new IllegalStateException("Connector has not been initialized");
        }

        return initializationContext;
    }

    @Override
    public void start(final FlowContext context) throws FlowUpdateException {
        final ProcessGroupLifecycle lifecycle = context.getRootGroup().getLifecycle();
        final CompletableFuture<Void> enableServicesFuture = lifecycle.enableControllerServices(
            ControllerServiceReferenceScope.INCLUDE_REFERENCED_SERVICES_ONLY,
            ComponentHierarchyScope.INCLUDE_CHILD_GROUPS);

        try {
            enableServicesFuture.get();
        } catch (final Exception e) {
            lifecycle.disableControllerServices(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS);
            throw new FlowUpdateException("Failed to enable Controller Services while starting Connector", e);
        }

        try {
            lifecycle.startProcessors(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get();
            lifecycle.startPorts(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get();
            lifecycle.startStatelessGroups(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get();
            lifecycle.startRemoteProcessGroups(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get();
        } catch (final Exception e) {
            logger.error("Failed to start components for {}", this, e);
            try {
                stop(context);
            } catch (final Exception stopException) {
                e.addSuppressed(new FlowUpdateException("Failed to stop Connector cleanly", stopException));
            }
        }
    }

    @Override
    public void stop(final FlowContext context) throws FlowUpdateException {
        try {
            stopAsync(context).get();
        } catch (final Exception e) {
            throw new FlowUpdateException("Failed to stop Connector", e);
        }
    }

    private CompletableFuture<Void> stopAsync(final FlowContext context) {
        final CompletableFuture<Void> result = new CompletableFuture<>();

        Thread.startVirtualThread(() -> {
            try {
                final ProcessGroupFacade rootGroup = context.getRootGroup();
                final ProcessGroupLifecycle lifecycle = rootGroup.getLifecycle();

                try {
                    lifecycle.stopProcessors(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get(1, TimeUnit.MINUTES);
                } catch (final TimeoutException e) {
                    final List<ProcessorFacade> running = findProcessors(rootGroup, processor ->
                        processor.getLifecycle().getState() != ProcessorState.STOPPED && processor.getLifecycle().getState() != ProcessorState.DISABLED);

                    if (!running.isEmpty()) {
                        getLogger().warn("After waiting 60 seconds for all Processors to stop, {} are still running. Terminating now.", running.size());
                        running.forEach(processor -> processor.getLifecycle().terminate());
                    }
                } catch (final ExecutionException e) {
                    throw new RuntimeException("Failed to stop all Processors", e.getCause());
                }

                lifecycle.stopPorts(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get(1, TimeUnit.MINUTES);
                lifecycle.stopRemoteProcessGroups(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get(1, TimeUnit.MINUTES);
                lifecycle.stopStatelessGroups(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get(2, TimeUnit.MINUTES);
                lifecycle.disableControllerServices(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get(2, TimeUnit.MINUTES);

                result.complete(null);
            } catch (final Exception e) {
                result.completeExceptionally(e);
            }
        });

        return result;
    }

    @Override
    public void prepareForUpdate(final FlowContext workingContext, final FlowContext activeContext) throws FlowUpdateException {
        final CompletableFuture<Void> future = stopAsync(activeContext);
        prepareUpdateFuture = future;

        try {
            future.get();
        } catch (final Exception e) {
            throw new FlowUpdateException("Failed to prepare Connector for update", e);
        }
    }

    /**
     * Drains all FlowFiles from the Connector instance.
     *
     * @param flowContext the FlowContext to use for drainage
     * @return a CompletableFuture that will be completed when drainage is complete
     */
    @Override
    public CompletableFuture<Void> drainFlowFiles(final FlowContext flowContext) {
        final CompletableFuture<Void> result = new CompletableFuture<>();
        final QueueSize initialQueueSize = flowContext.getRootGroup().getQueueSize();
        if (initialQueueSize.getObjectCount() == 0) {
            getLogger().debug("No FlowFiles to drain from Connector");
            result.complete(null);
            return result;
        }

        getLogger().info("Draining {} FlowFiles ({} bytes) from Connector",
            initialQueueSize.getObjectCount(), NumberFormat.getNumberInstance().format(initialQueueSize.getByteCount()));

        Thread.startVirtualThread(() -> {
            try {
                stopSourceComponents(flowContext).get();
                startNonSourceComponents(result, flowContext);

                if (!result.isDone()) {
                    completeDrain(result, flowContext, initialQueueSize);
                }
            } catch (final Exception e) {
                if (!result.isDone()) {
                    result.completeExceptionally(new RuntimeException("Failed while draining FlowFiles", e));
                }
            }
        });

        return result;
    }

    private void completeDrain(final CompletableFuture<Void> result, final FlowContext flowContext, final QueueSize initialQueueSize) {
        try {
            ensureDrainageUnblocked();
        } catch (final Exception e) {
            getLogger().warn("Failed to ensure drainage is unblocked when draining FlowFiles", e);
        }

        Exception failureReason = null;
        int iterations = 0;
        while (!isGroupDrained(flowContext.getRootGroup())) {
            if (result.isDone()) {
                getLogger().info("Drain cancelled: no longer waiting for FlowFiles to drain");
                break;
            }

            // Log the current queue size every 10 seconds (20 iterations of 500ms) so that it's clear
            // whether or not progress is being made.
            if (iterations++ % 20 == 0) {
                final QueueSize queueSize = flowContext.getRootGroup().getQueueSize();
                getLogger().info("Waiting for {} FlowFiles ({} bytes) to drain",
                    queueSize.getObjectCount(), NumberFormat.getNumberInstance().format(queueSize.getByteCount()));
            }

            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                failureReason = e;
                break;
            }
        }

        // Log completion unless the result was completed exceptionally or cancelled.
        if (!result.isDone()) {
            getLogger().info("All {} FlowFiles have drained from Connector", initialQueueSize.getObjectCount());
        }

        try {
            stop(flowContext);
        } catch (final Exception e) {
            getLogger().warn("Failed to stop source Processors after draining FlowFiles", e);
            if (failureReason == null) {
                failureReason = e;
            } else {
                failureReason.addSuppressed(e);
            }
        }

        if (failureReason != null && !result.isDone()) {
            result.completeExceptionally(new RuntimeException("Interrupted while waiting for " + AbstractConnector.this + " to drain", failureReason));
        }

        if (!result.isDone()) {
            result.complete(null);
        }
    }

    private void startNonSourceComponents(final CompletableFuture<Void> result, final FlowContext flowContext) {
        if (result.isDone()) {
            return;
        }

        final CompletableFuture<Void> enableServices = flowContext.getRootGroup().getLifecycle().enableControllerServices(
            ControllerServiceReferenceScope.INCLUDE_REFERENCED_SERVICES_ONLY,
            ComponentHierarchyScope.INCLUDE_CHILD_GROUPS);

        try {
            // Wait for all referenced services to be enabled.
            enableServices.get();

            if (!result.isDone()) {
                getLogger().info("Starting all non-source components to facilitate drainage of FlowFiles");
                startNonSourceComponents(flowContext).get();
            }
        } catch (final Exception e) {
            try {
                flowContext.getRootGroup().getLifecycle().disableControllerServices(ComponentHierarchyScope.INCLUDE_CHILD_GROUPS).get();
            } catch (final Exception e1) {
                e.addSuppressed(e1);
            }

            result.completeExceptionally(new RuntimeException("Failed to start non-source components while draining FlowFiles", e.getCause()));
        }
    }

    /**
     * <p>
     *     A method designed to be overridden by subclasses that need to ensure that any
     *     blockages to FlowFile drainage are removed. The default implementation is a no-op.
     *     Typical use cases include notifying Processors that block until a certain amount of data is queued up,
     *     or until certain conditions are met, that they should immediately allow data to flow through.
     * </p>
     */
    protected void ensureDrainageUnblocked() throws InvocationFailedException {
    }

    @Override
    public List<ConfigVerificationResult> verify(final FlowContext flowContext) {
        final List<ConfigVerificationResult> results = new ArrayList<>();

        final List<ConfigurationStep> configSteps = getConfigurationSteps();
        for (final ConfigurationStep configStep : configSteps) {
            final List<ConfigVerificationResult> stepResults = verifyConfigurationStep(configStep.getName(), Map.of(), flowContext);
            results.addAll(stepResults);
        }

        return results;
    }

    @Override
    public List<ValidationResult> validate(final FlowContext flowContext, final ConnectorValidationContext validationContext) {
        final ConnectorConfigurationContext configContext = flowContext.getConfigurationContext();
        final List<ValidationResult> results = new ArrayList<>();
        final List<ConfigurationStep> configurationSteps = getConfigurationSteps();

        for (final ConfigurationStep configurationStep : configurationSteps) {
            if (!isStepDependencySatisfied(configurationStep, configurationSteps, configContext)) {
                getLogger().debug("Skipping validation for Configuration Step [{}] because its dependencies are not satisfied", configurationStep.getName());
                continue;
            }

            results.addAll(validateConfigurationStep(configurationStep, configContext, validationContext));
        }

        // only run customValidate if regular validation is successful. This allows Processor developers to not have to check
        // if values are null or invalid so that they can focus only on the interaction between the properties, etc.
        if (results.isEmpty()) {
            final Collection<ValidationResult> customResults = customValidate(configContext);
            if (customResults != null) {
                for (final ValidationResult result : customResults) {
                    if (!result.isValid()) {
                        results.add(result);
                    }
                }
            }
        }

        return results;
    }

    protected List<ValidationResult> validateComponents(final FlowContext context, final ProcessGroupFacade group, final ConnectorValidationContext validationContext) {
        final List<ValidationResult> validationResults = new ArrayList<>();
        validateComponents(context, group, validationContext, validationResults);
        return validationResults;
    }

    private void validateComponents(final FlowContext context, final ProcessGroupFacade group, final ConnectorValidationContext validationContext,
            final List<ValidationResult> validationResults) {

        for (final ProcessorFacade processor : group.getProcessors()) {
            final List<ValidationResult> processorResults = processor.validate();
            for (final ValidationResult result : processorResults) {
                if (result.isValid()) {
                    continue;
                }

                validationResults.add(new ValidationResult.Builder()
                    .valid(false)
                    .subject(result.getSubject())
                    .input(result.getInput())
                    .explanation("Processor [%s] is invalid: %s".formatted(processor.getDefinition().getName(), result.getExplanation()))
                    .build());
            }
        }

        final Set<ControllerServiceFacade> referencedServices = group.getControllerServices(
            ControllerServiceReferenceScope.INCLUDE_REFERENCED_SERVICES_ONLY,
            ComponentHierarchyScope.IMMEDIATE_GROUP_ONLY);

        for (final ControllerServiceFacade service : referencedServices) {
            final List<ValidationResult> serviceResults = service.validate();
            for (final ValidationResult result : serviceResults) {
                if (result.isValid()) {
                    continue;
                }

                validationResults.add(new ValidationResult.Builder()
                    .valid(false)
                    .subject(result.getSubject())
                    .input(result.getInput())
                    .explanation("Controller Service [%s] is invalid: %s".formatted(service.getDefinition().getName(), result.getExplanation()))
                    .build());
            }
        }

        for (final ProcessGroupFacade childGroup : group.getProcessGroups()) {
            validateComponents(context, childGroup, validationContext, validationResults);
        }
    }

    protected boolean isGroupDrained(final ProcessGroupFacade group) {
        return group.getQueueSize().getObjectCount() == 0;
    }

    protected CompletableFuture<Void> stopSourceComponents(final FlowContext context) {
        return stopSourceComponents(context.getRootGroup());
    }

    private CompletableFuture<Void> stopSourceComponents(final ProcessGroupFacade group) {
        final List<CompletableFuture<Void>> stopFutures = new ArrayList<>();

        final List<ProcessorFacade> sourceProcessors = getSourceProcessors(group);
        for (final ProcessorFacade sourceProcessor : sourceProcessors) {
            stopFutures.add(sourceProcessor.getLifecycle().stop());
        }

        for (final ProcessGroupFacade childGroup : group.getProcessGroups()) {
            stopFutures.add(stopSourceComponents(childGroup));
        }

        return CompletableFuture.allOf(stopFutures.toArray(new CompletableFuture[0]));
    }

    protected CompletableFuture<Void> startNonSourceComponents(final FlowContext flowContext) {
        return startNonSourceComponents(flowContext.getRootGroup());
    }

    private CompletableFuture<Void> startNonSourceComponents(final ProcessGroupFacade group) {
        final List<CompletableFuture<Void>> startFutures = new ArrayList<>();

        final List<ProcessorFacade> nonSourceProcessors = getNonSourceProcessors(group);
        for (final ProcessorFacade nonSourceProcessor : nonSourceProcessors) {
            startFutures.add(nonSourceProcessor.getLifecycle().start());
        }

        final ProcessGroupLifecycle lifecycle = group.getLifecycle();
        startFutures.add(lifecycle.startPorts(ComponentHierarchyScope.IMMEDIATE_GROUP_ONLY));
        startFutures.add(lifecycle.startRemoteProcessGroups(ComponentHierarchyScope.IMMEDIATE_GROUP_ONLY));
        startFutures.add(lifecycle.startStatelessGroups(ComponentHierarchyScope.IMMEDIATE_GROUP_ONLY));

        for (final ProcessGroupFacade childGroup : group.getProcessGroups()) {
            startFutures.add(startNonSourceComponents(childGroup));
        }

        return CompletableFuture.allOf(startFutures.toArray(new CompletableFuture[0]));
    }

    protected List<ProcessorFacade> getSourceProcessors(final ProcessGroupFacade group) {
        final Set<String> nonSourceIds = getNonSourceProcessorIds(group);

        return findProcessors(group, processor -> !nonSourceIds.contains(processor.getDefinition().getIdentifier()));
    }

    protected List<ProcessorFacade> getNonSourceProcessors(final ProcessGroupFacade group) {
        final Set<String> nonSourceIds = getNonSourceProcessorIds(group);

        return findProcessors(group, processor -> nonSourceIds.contains(processor.getDefinition().getIdentifier()));
    }

    protected Set<String> getNonSourceProcessorIds(final ProcessGroupFacade group) {
        final Set<String> destinationIds = new HashSet<>();
        forEachConnection(group, conn -> {
            final VersionedConnection definition = conn.getDefinition();
            final String sourceId = definition.getSource().getId();
            final String destinationId = definition.getDestination().getId();
            if (!Objects.equals(sourceId, destinationId)) {
                destinationIds.add(destinationId);
            }
        });

        return destinationIds;
    }

    protected List<ProcessorFacade> findProcessors(final ProcessGroupFacade group, final Predicate<ProcessorFacade> filter) {
        final List<ProcessorFacade> matching = new ArrayList<>();
        findProcessors(group, filter, matching);
        return matching;
    }

    private void findProcessors(final ProcessGroupFacade group, final Predicate<ProcessorFacade> filter, final List<ProcessorFacade> found) {
        for (final ProcessorFacade processor : group.getProcessors()) {
            if (filter.test(processor)) {
                found.add(processor);
            }
        }

        for (final ProcessGroupFacade childGroup : group.getProcessGroups()) {
            findProcessors(childGroup, filter, found);
        }
    }

    private void forEachConnection(final ProcessGroupFacade group, final Consumer<ConnectionFacade> connectionConsumer) {
        for (final ConnectionFacade connection : group.getConnections()) {
            connectionConsumer.accept(connection);
        }

        for (final ProcessGroupFacade childGroup : group.getProcessGroups()) {
            forEachConnection(childGroup, connectionConsumer);
        }
    }


    @Override
    public List<ValidationResult> validateConfigurationStep(final ConfigurationStep configurationStep, final ConnectorConfigurationContext configurationContext,
            final ConnectorValidationContext validationContext) {

        final String stepName = configurationStep.getName();
        final List<ValidationResult> results = new ArrayList<>();

        // Build a set of all valid property names defined by this configuration step
        final List<ConnectorPropertyGroup> propertyGroups = configurationStep.getPropertyGroups();
        final Set<String> validPropertyNames = new HashSet<>();
        for (final ConnectorPropertyGroup propertyGroup : propertyGroups) {
            final List<ConnectorPropertyDescriptor> descriptors = propertyGroup.getProperties();
            for (final ConnectorPropertyDescriptor descriptor : descriptors) {
                validPropertyNames.add(descriptor.getName());
            }
        }

        // Check for any properties that have been set but are not defined by this configuration step
        final Set<String> configuredPropertyNames = configurationContext.getPropertyNames(stepName);
        for (final String configuredPropertyName : configuredPropertyNames) {
            if (!validPropertyNames.contains(configuredPropertyName)) {
                final String configuredValue = configurationContext.getProperty(stepName, configuredPropertyName).getValue();

                final ValidationResult invalidResult = new ValidationResult.Builder()
                    .valid(false)
                    .input(configuredValue)
                    .subject(configuredPropertyName)
                    .explanation("Property '" + configuredPropertyName + "' is not defined by Connector for Configuration Step '" + stepName + "'")
                    .build();
                results.add(invalidResult);
            }
        }

        for (final ConnectorPropertyGroup propertyGroup : propertyGroups) {
            final List<ConnectorPropertyDescriptor> descriptors = propertyGroup.getProperties();
            final Map<String, ConnectorPropertyDescriptor> descriptorMap = descriptors.stream()
                .collect(Collectors.toMap(ConnectorPropertyDescriptor::getName, Function.identity()));

            final Function<String, ConnectorPropertyValue> propertyValueLookup = name -> configurationContext.getProperty(stepName, name);

            for (final ConnectorPropertyDescriptor descriptor : descriptors) {
                final boolean dependencySatisfied = isDependencySatisfied(descriptor, descriptorMap::get, propertyValueLookup);

                // If the property descriptor's dependency is not satisfied, the property does not need to be considered, as it's not relevant to the
                if (!dependencySatisfied) {
                    continue;
                }

                final ConnectorPropertyValue propertyValue = configurationContext.getProperty(stepName, descriptor.getName());
                if (propertyValue == null || !propertyValue.isSet()) {
                    if (descriptor.isRequired()) {
                        final ValidationResult invalidResult = new ValidationResult.Builder()
                            .valid(false)
                            .input(null)
                            .subject(descriptor.getName())
                            .explanation(descriptor.getName() + " is required")
                            .build();
                        results.add(invalidResult);
                    }

                    continue;
                }

                final ValidationResult result = descriptor.validate(stepName, propertyGroup.getName(), propertyValue.getValue(), validationContext);
                if (!result.isValid()) {
                    results.add(result);
                }
            }
        }

        return results;
    }

    private boolean isDependencySatisfied(final ConnectorPropertyDescriptor propertyDescriptor, final Function<String, ConnectorPropertyDescriptor> propertyDescriptorLookup,
            final Function<String, ConnectorPropertyValue> propertyValueLookup) {

        return isDependencySatisfied(propertyDescriptor, propertyDescriptorLookup, propertyValueLookup, new HashSet<>());
    }

    private boolean isDependencySatisfied(final ConnectorPropertyDescriptor propertyDescriptor, final Function<String, ConnectorPropertyDescriptor> propertyDescriptorLookup,
            final Function<String, ConnectorPropertyValue> propertyValueLookup, final Set<String> propertiesSeen) {

        final Set<ConnectorPropertyDependency> dependencies = propertyDescriptor.getDependencies();
        if (dependencies.isEmpty()) {
            return true;
        }

        final boolean added = propertiesSeen.add(propertyDescriptor.getName());
        if (!added) {
            return false;
        }

        try {
            for (final ConnectorPropertyDependency dependency : dependencies) {
                final String dependencyName = dependency.getPropertyName();

                // Check if the property being depended upon has its dependencies satisfied.
                final ConnectorPropertyDescriptor dependencyDescriptor = propertyDescriptorLookup.apply(dependencyName);
                if (dependencyDescriptor == null) {
                    return false;
                }

                final ConnectorPropertyValue propertyValue = propertyValueLookup.apply(dependencyDescriptor.getName());
                final String dependencyValue = propertyValue == null ? dependencyDescriptor.getDefaultValue() : propertyValue.getValue();
                if (dependencyValue == null) {
                    return false;
                }

                final boolean transitiveDependencySatisfied = isDependencySatisfied(dependencyDescriptor, propertyDescriptorLookup, propertyValueLookup, propertiesSeen);
                if (!transitiveDependencySatisfied) {
                    return false;
                }

                // Check if the property being depended upon is set to one of the values that satisfies this dependency.
                // If the dependency has no dependent values, then any non-null value satisfies the dependency.
                // The value is already known to be non-null due to the check above.
                final Set<String> dependentValues = dependency.getDependentValues();
                if (dependentValues != null && !dependentValues.contains(dependencyValue)) {
                    return false;
                }
            }

            return true;
        } finally {
            propertiesSeen.remove(propertyDescriptor.getName());
        }
    }

    private boolean isStepDependencySatisfied(final ConfigurationStep step, final List<ConfigurationStep> allSteps,
            final ConnectorConfigurationContext configContext) {

        final Set<ConfigurationStepDependency> dependencies = step.getDependencies();
        if (dependencies.isEmpty()) {
            return true;
        }

        for (final ConfigurationStepDependency dependency : dependencies) {
            final String dependentStepName = dependency.getStepName();
            final String dependentPropertyName = dependency.getPropertyName();

            final ConfigurationStep dependentStep = findStepByName(allSteps, dependentStepName);
            if (dependentStep == null) {
                getLogger().debug("Dependency of step {} is not satisfied because it depends on step {} which could not be found", step.getName(), dependentStepName);
                return false;
            }

            final ConnectorPropertyDescriptor dependentProperty = findPropertyInStep(dependentStep, dependentPropertyName);
            if (dependentProperty == null) {
                getLogger().debug("Dependency of step {} is not satisfied because it depends on property {} in step {} which could not be found",
                    step.getName(), dependentPropertyName, dependentStepName);
                return false;
            }

            final ConnectorPropertyValue propertyValue = configContext.getProperty(dependentStepName, dependentPropertyName);
            final String value = propertyValue == null ? dependentProperty.getDefaultValue() : propertyValue.getValue();

            final Set<String> dependentValues = dependency.getDependentValues();
            if (dependentValues == null) {
                // Dependency is satisfied as long as the property has any value configured.
                if (value == null) {
                    return false;
                }

                continue;
            }

            if (!dependentValues.contains(value)) {
                return false;
            }
        }

        return true;
    }

    private ConfigurationStep findStepByName(final List<ConfigurationStep> steps, final String stepName) {
        for (final ConfigurationStep step : steps) {
            if (step.getName().equals(stepName)) {
                return step;
            }
        }
        return null;
    }

    private ConnectorPropertyDescriptor findPropertyInStep(final ConfigurationStep step, final String propertyName) {
        for (final ConnectorPropertyGroup group : step.getPropertyGroups()) {
            for (final ConnectorPropertyDescriptor descriptor : group.getProperties()) {
                if (descriptor.getName().equals(propertyName)) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    @Override
    public final void onConfigurationStepConfigured(final String stepName, final FlowContext workingContext) throws FlowUpdateException {
        onStepConfigured(stepName, workingContext);
    }

    @Override
    public void abortUpdate(final FlowContext workingContext, final Throwable throwable) {
        if (prepareUpdateFuture != null && !prepareUpdateFuture.isDone()) {
            prepareUpdateFuture.completeExceptionally(throwable);
        }
    }

    @Override
    public List<DescribedValue> fetchAllowableValues(final String stepName, final String propertyName, final FlowContext flowContext) {
        throw new UnsupportedOperationException("Property %s in Configuration Step %s does not support fetching Allowable Values.".formatted(propertyName, stepName));
    }

    @Override
    public List<DescribedValue> fetchAllowableValues(final String stepName, final String propertyName, final FlowContext flowContext, final String filter) {
        final List<DescribedValue> allowableValues = fetchAllowableValues(stepName, propertyName, flowContext);
        if (filter == null || filter.isEmpty()) {
            return allowableValues;
        } else {
            return allowableValues.stream()
                .filter(value -> value.getValue().toLowerCase().contains(filter.toLowerCase()) || value.getValue().toUpperCase().contains(filter.toUpperCase()))
                .toList();
        }
    }


    /**
     * No-op implementation that allows concrete subclasses to perform validation of property configuration
     *
     * @param context the context that should be used for validation
     * @return a collection of validation results indicating any problems with the configuration.
     */
    protected Collection<ValidationResult> customValidate(final ConnectorConfigurationContext context) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return description;
    }
}
