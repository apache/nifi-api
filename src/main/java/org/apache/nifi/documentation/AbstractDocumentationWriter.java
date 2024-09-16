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
package org.apache.nifi.documentation;

import org.apache.nifi.annotation.behavior.DynamicProperties;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.DynamicRelationship;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.PrimaryNodeOnly;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.Restricted;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.behavior.SupportsSensitiveDynamicProperties;
import org.apache.nifi.annotation.behavior.SystemResourceConsideration;
import org.apache.nifi.annotation.behavior.TriggerSerially;
import org.apache.nifi.annotation.behavior.TriggerWhenAnyDestinationAvailable;
import org.apache.nifi.annotation.behavior.TriggerWhenEmpty;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.configuration.DefaultSchedule;
import org.apache.nifi.annotation.configuration.DefaultSettings;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.DeprecationNotice;
import org.apache.nifi.annotation.documentation.MultiProcessorUseCase;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.documentation.UseCase;
import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.documentation.init.DocumentationControllerServiceInitializationContext;
import org.apache.nifi.documentation.init.DocumentationFlowAnalysisRuleInitializationContext;
import org.apache.nifi.documentation.init.DocumentationParameterProviderInitializationContext;
import org.apache.nifi.documentation.init.DocumentationProcessorInitializationContext;
import org.apache.nifi.documentation.init.DocumentationReportingInitializationContext;
import org.apache.nifi.flowanalysis.FlowAnalysisRule;
import org.apache.nifi.parameter.ParameterProvider;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.reporting.ReportingTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for DocumentationWriter that simplifies iterating over all information for a component, creating a separate method
 * for each, to ensure that implementations properly override all methods and therefore properly account for all information about
 * a component.
 *
 * Please note that while this class lives within the nifi-api, it is provided primarily as a means for documentation components within
 * the NiFi NAR Maven Plugin. Its home is the nifi-api, however, because the API is needed in order to extract the relevant information and
 * the NAR Maven Plugin cannot have a direct dependency on nifi-api (doing so would cause a circular dependency). By having this homed within
 * the nifi-api, the Maven plugin is able to discover the class dynamically and invoke the one or two methods necessary to create the documentation.
 *
 * This is a new capability in 1.9.0 in preparation for the Extension Registry and therefore, you should
 * <b>NOTE WELL:</b> At this time, while this class is part of nifi-api, it is still evolving and may change in a non-backward-compatible manner or even be
 * removed from one incremental release to the next. Use at your own risk!
 */
public abstract class AbstractDocumentationWriter implements ExtensionDocumentationWriter {

    @Override
    public void initialize(final ConfigurableComponent component) {
        try {
            if (component instanceof Processor) {
                initialize((Processor) component);
            } else if (component instanceof ControllerService) {
                initialize((ControllerService) component);
            } else if (component instanceof ReportingTask) {
                initialize((ReportingTask) component);
            } else if (component instanceof FlowAnalysisRule) {
                initialize((FlowAnalysisRule) component);
            } else if (component instanceof ParameterProvider) {
                initialize((ParameterProvider) component);
            }
        } catch (final InitializationException ie) {
            throw new RuntimeException("Failed to initialize " + component, ie);
        }
    }

    protected void initialize(final Processor processor) {
        processor.initialize(new DocumentationProcessorInitializationContext());
    }

    protected void initialize(final ControllerService service) throws InitializationException {
        service.initialize(new DocumentationControllerServiceInitializationContext());
    }

    protected void initialize(final ReportingTask reportingTask) throws InitializationException {
        reportingTask.initialize(new DocumentationReportingInitializationContext());
    }

    protected void initialize(final FlowAnalysisRule flowAnalysisRule) throws InitializationException {
        flowAnalysisRule.initialize(new DocumentationFlowAnalysisRuleInitializationContext());
    }

    protected void initialize(final ParameterProvider parameterProvider) throws InitializationException {
        parameterProvider.initialize(new DocumentationParameterProviderInitializationContext());
    }

    @Override
    public final void write(final ConfigurableComponent component) throws IOException {
        write(component, Collections.emptyList(), Collections.emptyMap());
    }

    @Override
    public final void write(final ConfigurableComponent component, final Collection<ServiceAPI> providedServices, Map<String, ServiceAPI> propertyServices) throws IOException {
        writeHeader(component);
        writeBody(component, propertyServices);

        if (providedServices != null && component instanceof ControllerService) {
            writeProvidedServices(providedServices);
        }

        writeFooter(component);
    }

    protected void writeBody(final ConfigurableComponent component, Map<String, ServiceAPI> propertyServices) throws IOException {
        writeExtensionName(component.getClass().getName());
        writeExtensionType(getExtensionType(component));
        writeDeprecationNotice(component.getClass().getAnnotation(DeprecationNotice.class));
        writeDescription(getDescription(component));
        writeTags(getTags(component));
        writeProperties(component.getPropertyDescriptors(), propertyServices);
        writeDynamicProperties(getDynamicProperties(component));
        writeSupportsSensitiveDynamicProperties(component.getClass().getAnnotation(SupportsSensitiveDynamicProperties.class));

        if (component instanceof Processor) {
            final Processor processor = (Processor) component;

            writeRelationships(processor.getRelationships());
            writeDynamicRelationship(getDynamicRelationship(processor));
            writeReadsAttributes(getReadsAttributes(processor));
            writeWritesAttributes(getWritesAttributes(processor));

            writeTriggerSerially(processor.getClass().getAnnotation(TriggerSerially.class));
            writeTriggerWhenEmpty(processor.getClass().getAnnotation(TriggerWhenEmpty.class));
            writeTriggerWhenAnyDestinationAvailable(processor.getClass().getAnnotation(TriggerWhenAnyDestinationAvailable.class));
            writeSupportsBatching(processor.getClass().getAnnotation(SupportsBatching.class));
            writePrimaryNodeOnly(processor.getClass().getAnnotation(PrimaryNodeOnly.class));
            writeSideEffectFree(processor.getClass().getAnnotation(SideEffectFree.class));
            writeDefaultSettings(processor.getClass().getAnnotation(DefaultSettings.class));
        }

        writeStatefulInfo(component.getClass().getAnnotation(Stateful.class));
        writeRestrictedInfo(component.getClass().getAnnotation(Restricted.class));
        writeInputRequirementInfo(getInputRequirement(component));
        writeSystemResourceConsiderationInfo(getSystemResourceConsiderations(component));
        writeUseCases(getUseCases(component));
        writeMultiProcessorUseCases(getMultiProcessorUseCases(component));
        writeSeeAlso(component.getClass().getAnnotation(SeeAlso.class));
        writeDefaultSchedule(component.getClass().getAnnotation(DefaultSchedule.class));
    }

    protected String getDescription(final ConfigurableComponent component) {
        final CapabilityDescription capabilityDescription = component.getClass().getAnnotation(CapabilityDescription.class);
        if (capabilityDescription == null) {
            return null;
        }

        return capabilityDescription.value();
    }

    protected List<String> getTags(final ConfigurableComponent component) {
        final Tags tags = component.getClass().getAnnotation(Tags.class);
        if (tags == null) {
            return Collections.emptyList();
        }

        final String[] tagValues = tags.value();
        return tagValues == null ? Collections.emptyList() : Arrays.asList(tagValues);
    }

    protected List<DynamicProperty> getDynamicProperties(ConfigurableComponent configurableComponent) {
        final List<DynamicProperty> dynamicProperties = new ArrayList<>();
        final DynamicProperties dynProps = configurableComponent.getClass().getAnnotation(DynamicProperties.class);
        if (dynProps != null) {
            Collections.addAll(dynamicProperties, dynProps.value());
        }

        final DynamicProperty dynProp = configurableComponent.getClass().getAnnotation(DynamicProperty.class);
        if (dynProp != null) {
            dynamicProperties.add(dynProp);
        }

        return dynamicProperties;
    }


    private DynamicRelationship getDynamicRelationship(Processor processor) {
        return processor.getClass().getAnnotation(DynamicRelationship.class);
    }


    private List<ReadsAttribute> getReadsAttributes(final Processor processor) {
        final List<ReadsAttribute> attributes = new ArrayList<>();

        final ReadsAttributes readsAttributes = processor.getClass().getAnnotation(ReadsAttributes.class);
        if (readsAttributes != null) {
            Collections.addAll(attributes, readsAttributes.value());
        }

        final ReadsAttribute readsAttribute = processor.getClass().getAnnotation(ReadsAttribute.class);
        if (readsAttribute != null) {
            attributes.add(readsAttribute);
        }

        return attributes;
    }


    private List<WritesAttribute> getWritesAttributes(Processor processor) {
        List<WritesAttribute> attributes = new ArrayList<>();

        WritesAttributes writesAttributes = processor.getClass().getAnnotation(WritesAttributes.class);
        if (writesAttributes != null) {
            Collections.addAll(attributes, writesAttributes.value());
        }

        WritesAttribute writeAttribute = processor.getClass().getAnnotation(WritesAttribute.class);
        if (writeAttribute != null) {
            attributes.add(writeAttribute);
        }

        return attributes;
    }

    private InputRequirement.Requirement getInputRequirement(final ConfigurableComponent component) {
        final InputRequirement annotation = component.getClass().getAnnotation(InputRequirement.class);
        return annotation == null ? null : annotation.value();
    }

    private List<SystemResourceConsideration> getSystemResourceConsiderations(final ConfigurableComponent component) {
        SystemResourceConsideration[] systemResourceConsiderations = component.getClass().getAnnotationsByType(SystemResourceConsideration.class);
        if (systemResourceConsiderations.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(systemResourceConsiderations);
    }

    private List<UseCase> getUseCases(final ConfigurableComponent component) {
        UseCase[] useCases = component.getClass().getAnnotationsByType(UseCase.class);
        if (useCases.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(useCases);
    }

    private List<MultiProcessorUseCase> getMultiProcessorUseCases(final ConfigurableComponent component) {
        MultiProcessorUseCase[] useCases = component.getClass().getAnnotationsByType(MultiProcessorUseCase.class);
        if (useCases.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(useCases);
    }

    protected ExtensionType getExtensionType(final ConfigurableComponent component) {
        if (component instanceof Processor) {
            return ExtensionType.PROCESSOR;
        }
        if (component instanceof ControllerService) {
            return ExtensionType.CONTROLLER_SERVICE;
        }
        if (component instanceof ReportingTask) {
            return ExtensionType.REPORTING_TASK;
        }
        if (component instanceof FlowAnalysisRule) {
            return ExtensionType.FLOW_ANALYSIS_RULE;
        }
        if (component instanceof ParameterProvider) {
            return ExtensionType.PARAMETER_PROVIDER;
        }
        throw new AssertionError("Encountered unknown Configurable Component Type for " + component);
    }


    protected abstract void writeHeader(ConfigurableComponent component) throws IOException;

    protected abstract void writeExtensionName(String extensionName) throws IOException;

    protected abstract void writeExtensionType(ExtensionType extensionType) throws IOException;

    protected abstract void writeDeprecationNotice(final DeprecationNotice deprecationNotice) throws IOException;


    protected abstract void writeDescription(String description) throws IOException;

    protected abstract void writeTags(List<String> tags) throws IOException;

    protected abstract void writeProperties(List<PropertyDescriptor> properties, Map<String, ServiceAPI> propertyServices) throws IOException;

    protected abstract void writeDynamicProperties(List<DynamicProperty> dynamicProperties) throws IOException;

    protected abstract void writeStatefulInfo(Stateful stateful) throws IOException;

    protected abstract void writeRestrictedInfo(Restricted restricted) throws IOException;

    protected abstract void writeInputRequirementInfo(InputRequirement.Requirement requirement) throws IOException;

    protected abstract void writeSystemResourceConsiderationInfo(List<SystemResourceConsideration> considerations) throws IOException;

    protected abstract void writeSeeAlso(SeeAlso seeAlso) throws IOException;

    protected abstract void writeUseCases(List<UseCase> useCases) throws IOException;

    protected abstract void writeMultiProcessorUseCases(List<MultiProcessorUseCase> useCases) throws IOException;

    protected abstract void writeDefaultSchedule(DefaultSchedule defaultSchedule) throws IOException;

    // Processor-specific methods
    protected abstract void writeRelationships(Set<Relationship> relationships) throws IOException;

    protected abstract void writeDynamicRelationship(DynamicRelationship dynamicRelationship) throws IOException;

    protected abstract void writeReadsAttributes(List<ReadsAttribute> attributes) throws IOException;

    protected abstract void writeWritesAttributes(List<WritesAttribute> attributes) throws IOException;

    protected abstract void writeTriggerSerially(TriggerSerially triggerSerially) throws IOException;

    protected abstract void writeTriggerWhenEmpty(TriggerWhenEmpty triggerWhenEmpty) throws IOException;

    protected abstract void writeTriggerWhenAnyDestinationAvailable(TriggerWhenAnyDestinationAvailable triggerWhenAnyDestinationAvailable) throws IOException;

    protected abstract void writeSupportsBatching(SupportsBatching supportsBatching) throws IOException;

    protected abstract void writeSupportsSensitiveDynamicProperties(SupportsSensitiveDynamicProperties supportsSensitiveDynamicProperties) throws IOException;

    protected abstract void writePrimaryNodeOnly(PrimaryNodeOnly primaryNodeOnly) throws IOException;

    protected abstract void writeSideEffectFree(SideEffectFree sideEffectFree) throws IOException;

    protected abstract void writeDefaultSettings(DefaultSettings defaultSettings) throws IOException;

    // ControllerService-specific methods
    protected abstract void writeProvidedServices(Collection<ServiceAPI> providedServices) throws IOException;

    protected abstract void writeFooter(ConfigurableComponent component) throws IOException;

}
