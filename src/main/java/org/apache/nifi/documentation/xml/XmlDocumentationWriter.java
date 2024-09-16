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
package org.apache.nifi.documentation.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.DynamicRelationship;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.PrimaryNodeOnly;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.Restricted;
import org.apache.nifi.annotation.behavior.Restriction;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.behavior.SupportsSensitiveDynamicProperties;
import org.apache.nifi.annotation.behavior.SystemResourceConsideration;
import org.apache.nifi.annotation.behavior.TriggerSerially;
import org.apache.nifi.annotation.behavior.TriggerWhenAnyDestinationAvailable;
import org.apache.nifi.annotation.behavior.TriggerWhenEmpty;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.configuration.DefaultSchedule;
import org.apache.nifi.annotation.configuration.DefaultSettings;
import org.apache.nifi.annotation.documentation.DeprecationNotice;
import org.apache.nifi.annotation.documentation.MultiProcessorUseCase;
import org.apache.nifi.annotation.documentation.ProcessorConfiguration;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.UseCase;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.components.PropertyDependency;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.RequiredPermission;
import org.apache.nifi.components.resource.ResourceDefinition;
import org.apache.nifi.components.resource.ResourceType;
import org.apache.nifi.documentation.AbstractDocumentationWriter;
import org.apache.nifi.documentation.ExtensionType;
import org.apache.nifi.documentation.ServiceAPI;
import org.apache.nifi.processor.Relationship;

/**
 * XML-based implementation of DocumentationWriter
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
public class XmlDocumentationWriter extends AbstractDocumentationWriter {
    private final XMLStreamWriter writer;

    public XmlDocumentationWriter(final OutputStream out) throws XMLStreamException {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
    }

    public XmlDocumentationWriter(final XMLStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    protected void writeHeader(final ConfigurableComponent component) throws IOException {
        writeStartElement("extension");
    }

    @Override
    protected void writeExtensionName(final String extensionName) throws IOException {
        writeTextElement("name", extensionName);
    }

    @Override
    protected void writeExtensionType(final ExtensionType extensionType) throws IOException {
        writeTextElement("type", extensionType.name());
    }

    @Override
    protected void writeDeprecationNotice(final DeprecationNotice deprecationNotice) throws IOException {
        if (deprecationNotice == null) {
            return;
        }

        final Class[] classes = deprecationNotice.alternatives();
        final String[] classNames = deprecationNotice.classNames();

        final Set<String> alternatives = new LinkedHashSet<>();
        if (classes != null) {
            for (final Class alternativeClass : classes) {
                alternatives.add(alternativeClass.getName());
            }
        }

        if (classNames != null) {
            Collections.addAll(alternatives, classNames);
        }

        writeDeprecationNotice(deprecationNotice.reason(), alternatives);
    }

    private void writeDeprecationNotice(final String reason, final Set<String> alternatives) throws IOException {
        writeStartElement("deprecationNotice");

        writeTextElement("reason", reason);
        writeTextArray("alternatives", "alternative", alternatives);

        writeEndElement();
    }


    @Override
    protected void writeDescription(final String description) throws IOException {
        if (description == null) {
            return;
        }
        writeTextElement("description", description);
    }

    @Override
    protected void writeTags(final List<String> tags) throws IOException {
        if (tags == null) {
            return;
        }
        writeTextArray("tags", "tag", tags);
    }

    @Override
    protected void writeProperties(final List<PropertyDescriptor> properties, Map<String, ServiceAPI> propertyServices) throws IOException {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        writeStartElement("properties");
        for (final PropertyDescriptor property : properties) {
            writeProperty(property, propertyServices);
        }
        writeEndElement();
    }

    private void writeProperty(final PropertyDescriptor property, Map<String, ServiceAPI> propertyServices) throws IOException {
        writeStartElement("property");

        writeTextElement("name", property.getName());
        writeTextElement("displayName", property.getDisplayName());
        writeTextElement("description", property.getDescription());
        if (property.getDefaultValue() != null) {
            writeTextElement("defaultValue", property.getDefaultValue());
        }

        if (property.getControllerServiceDefinition() != null) {
            writeStartElement("controllerServiceDefinition");

            final ServiceAPI serviceAPI = propertyServices.get(property.getName());
            if (serviceAPI != null) {
                writeTextElement("className", serviceAPI.getClassName());
                writeTextElement("groupId", serviceAPI.getGroupId());
                writeTextElement("artifactId", serviceAPI.getArtifactId());
                writeTextElement("version", serviceAPI.getVersion());
            } else {
                writeTextElement("className", property.getControllerServiceDefinition().getName());
                writeTextElement("groupId", "unknown");
                writeTextElement("artifactId", "unknown");
                writeTextElement("version", "unknown");
            }

            writeEndElement();
        }

        if (property.getAllowableValues() != null && !property.getAllowableValues().isEmpty()) {
            writeArray("allowableValues", property.getAllowableValues(), this::writeAllowableValue);
        }

        writeBooleanElement("required", property.isRequired());
        writeBooleanElement("sensitive", property.isSensitive());
        writeBooleanElement("expressionLanguageSupported", property.isExpressionLanguageSupported());
        if (property.getExpressionLanguageScope() != null) {
            writeTextElement("expressionLanguageScope", property.getExpressionLanguageScope().name());
        }
        writeBooleanElement("dynamicallyModifiesClasspath", property.isDynamicClasspathModifier());
        writeBooleanElement("dynamic", property.isDynamic());
        writeResourceDefinition(property.getResourceDefinition());
        writeDependencies(property);

        writeEndElement();
    }

    private void writeResourceDefinition(final ResourceDefinition resourceDefinition) throws IOException {
        if (resourceDefinition == null) {
            return;
        }

        writeStartElement("resourceDefinition");
        writeTextElement("cardinality", resourceDefinition.getCardinality().name());
        writeArray("resourceTypes", resourceDefinition.getResourceTypes(), this::writeResourceType);
        writeEndElement();
    }

    private void writeResourceType(final ResourceType resourceType) throws IOException {
        writeTextElement("resourceType", resourceType.name());
    }

    private void writeAllowableValue(final AllowableValue allowableValue) throws IOException {
        writeStartElement("allowableValue");
        writeTextElement("displayName", allowableValue.getDisplayName());
        writeTextElement("value", allowableValue.getValue());
        writeTextElement("description", allowableValue.getDescription());
        writeEndElement();
    }

    private void writeDependencies(final PropertyDescriptor propertyDescriptor) throws IOException {
        final Set<PropertyDependency> dependencies = propertyDescriptor.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        writeStartElement("dependencies");

        for (final PropertyDependency dependency : dependencies) {
            writeStartElement("dependency");
            writeTextElement("propertyName", dependency.getPropertyName());
            writeTextElement("propertyDisplayName", dependency.getPropertyDisplayName());

            final Set<String> dependentValues = dependency.getDependentValues();
            if (dependentValues != null) {
                writeStartElement("dependentValues");
                for (final String dependentValue : dependentValues) {
                    writeTextElement("dependentValue", dependentValue);
                }
                writeEndElement();
            }

            writeEndElement();
        }

        writeEndElement();
    }

    @Override
    protected void writeDynamicProperties(final List<DynamicProperty> dynamicProperties) throws IOException {
        if (dynamicProperties == null || dynamicProperties.isEmpty()) {
            return;
        }
        writeArray("dynamicProperties", dynamicProperties, this::writeDynamicProperty);
    }

    private void writeDynamicProperty(final DynamicProperty property) throws IOException {
        writeStartElement("dynamicProperty");

        writeTextElement("name", property.name());
        writeTextElement("value", property.value());
        writeTextElement("description", property.description());
        writeTextElement("expressionLanguageScope", property.expressionLanguageScope() == null ? null : property.expressionLanguageScope().name());

        writeEndElement();
    }

    @Override
    protected void writeStatefulInfo(final Stateful stateful) throws IOException {
        if (stateful == null) {
            return;
        }

        writeStartElement("stateful");
        writeTextElement("description", stateful.description());
        writeArray("scopes", Arrays.asList(stateful.scopes()), scope -> writeTextElement("scope", scope.name()));
        writeEndElement();
    }

    @Override
    protected void writeRestrictedInfo(final Restricted restricted) throws IOException {
        if (restricted == null) {
            return;
        }

        writeStartElement("restricted");

        if (restricted.value() != null && !restricted.value().isEmpty()) {
            writeTextElement("generalRestrictionExplanation", restricted.value());
        }

        final Restriction[] restrictions = restricted.restrictions();
        if (restrictions != null) {
            writeArray("restrictions", Arrays.asList(restrictions), this::writeRestriction);
        }

        writeEndElement();
    }

    private void writeRestriction(final Restriction restriction) throws IOException {
        writeStartElement("restriction");

        final RequiredPermission permission = restriction.requiredPermission();
        final String label = permission == null ? null : permission.getPermissionLabel();
        writeTextElement("requiredPermission", label);
        writeTextElement("explanation", restriction.explanation());

        writeEndElement();
    }

    @Override
    protected void writeInputRequirementInfo(final InputRequirement.Requirement requirement) throws IOException {
        if (requirement == null) {
            return;
        }
        writeTextElement("inputRequirement", requirement.name());
    }

    @Override
    protected void writeSystemResourceConsiderationInfo(final List<SystemResourceConsideration> considerations) throws IOException {
        if (considerations == null || considerations.isEmpty()) {
            return;
        }
        writeArray("systemResourceConsiderations", considerations, this::writeSystemResourceConsideration);
    }

    private void writeSystemResourceConsideration(final SystemResourceConsideration consideration) throws IOException {
        writeStartElement("systemResourceConsideration");

        writeTextElement("resource", consideration.resource() == null ? null : consideration.resource().name());
        writeTextElement("description", consideration.description());

        writeEndElement();
    }

    @Override
    protected void writeSeeAlso(final SeeAlso seeAlso) throws IOException {
        if (seeAlso == null) {
            return;
        }

        final Class[] classes = seeAlso.value();
        final String[] classNames = seeAlso.classNames();

        final Set<String> toSee = new LinkedHashSet<>();
        if (classes != null) {
            for (final Class classToSee : classes) {
                toSee.add(classToSee.getName());
            }
        }

        if (classNames != null) {
            Collections.addAll(toSee, classNames);
        }

        writeTextArray("seeAlso", "see", toSee);
    }

    @Override
    protected void writeUseCases(final List<UseCase> useCases) throws IOException {
        if (useCases.isEmpty()) {
            return;
        }

        writeArray("useCases", useCases, this::writeUseCase);
    }

    private void writeUseCase(final UseCase useCase) throws IOException {
        writeStartElement("useCase");

        writeTextElement("description", useCase.description());
        writeTextElement("notes", useCase.notes());
        writeTextArray("keywords", "keyword", Arrays.asList(useCase.keywords()));
        writeTextElement("inputRequirement", useCase.inputRequirement().name());
        writeTextElement("configuration", useCase.configuration());

        writeEndElement();
    }

    protected void writeMultiProcessorUseCases(final List<MultiProcessorUseCase> multiProcessorUseCases) throws IOException {
        if (multiProcessorUseCases.isEmpty()) {
            return;
        }

        writeArray("multiProcessorUseCases", multiProcessorUseCases, this::writeMultiProcessorUseCase);
    }

    private void writeMultiProcessorUseCase(final MultiProcessorUseCase useCase) throws IOException {
        writeStartElement("multiProcessorUseCase");

        writeTextElement("description", useCase.description());
        writeTextElement("notes", useCase.notes());
        writeTextArray("keywords", "keyword", Arrays.asList(useCase.keywords()));

        writeArray("processorConfigurations", Arrays.asList(useCase.configurations()), this::writeUseCaseComponent);

        writeEndElement();
    }

    private void writeUseCaseComponent(final ProcessorConfiguration processorConfig) throws IOException {
        writeStartElement("processorConfiguration");

        String processorClassName = processorConfig.processorClassName();
        if (processorClassName.isEmpty()) {
            processorClassName = processorConfig.processorClass().getName();
        }

        writeTextElement("processorClassName", processorClassName);
        writeTextElement("configuration", processorConfig.configuration());

        writeEndElement();
    }


    @Override
    protected void writeRelationships(final Set<Relationship> relationships) throws IOException {
        if (relationships == null || relationships.isEmpty()) {
            return;
        }

        writeArray("relationships", relationships, rel -> {
            writeStartElement("relationship");

            writeTextElement("name", rel.getName());
            writeTextElement("description", rel.getDescription());
            writeBooleanElement("autoTerminated", rel.isAutoTerminated());

            writeEndElement();
        });
    }

    @Override
    protected void writeDynamicRelationship(final DynamicRelationship dynamicRelationship) throws IOException {
        if (dynamicRelationship == null) {
            return;
        }

        writeStartElement("dynamicRelationship");
        writeTextElement("name", dynamicRelationship.name());
        writeTextElement("description", dynamicRelationship.description());
        writeEndElement();
    }

    @Override
    protected void writeReadsAttributes(final List<ReadsAttribute> attributes) throws IOException {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        writeArray("readsAttributes", attributes, this::writeReadsAttribute);
    }

    private void writeReadsAttribute(final ReadsAttribute attribute) throws IOException {
        writeStartElement("readsAttribute");
        writeTextElement("name", attribute.attribute());
        writeTextElement("description", attribute.description());
        writeEndElement();
    }

    @Override
    protected void writeWritesAttributes(final List<WritesAttribute> attributes) throws IOException {
        if (attributes == null) {
            return;
        }
        writeArray("writesAttributes", attributes, this::writeWritesAttribute);
    }

    private void writeWritesAttribute(final WritesAttribute attribute) throws IOException {
        writeStartElement("writesAttribute");
        writeTextElement("name", attribute.attribute());
        writeTextElement("description", attribute.description());
        writeEndElement();
    }

    @Override
    protected void writeTriggerSerially(TriggerSerially triggerSerially) throws IOException {
        if (triggerSerially == null) {
            return;
        }
        writeBooleanElement("triggerSerially", true);
    }

    @Override
    protected void writeTriggerWhenEmpty(TriggerWhenEmpty triggerWhenEmpty) throws IOException {
        if (triggerWhenEmpty == null) {
            return;
        }
        writeBooleanElement("triggerWhenEmpty", true);
    }

    @Override
    protected void writeTriggerWhenAnyDestinationAvailable(TriggerWhenAnyDestinationAvailable triggerWhenAnyDestinationAvailable) throws IOException {
        if (triggerWhenAnyDestinationAvailable == null) {
            return;
        }
        writeBooleanElement("triggerWhenAnyDestinationAvailable", true);
    }

    @Override
    protected void writeSupportsBatching(SupportsBatching supportsBatching) throws IOException {
        if (supportsBatching == null) {
            return;
        }
        writeBooleanElement("supportsBatching", true);
    }

    @Override
    protected void writeSupportsSensitiveDynamicProperties(final SupportsSensitiveDynamicProperties supportsSensitiveDynamicProperties) throws IOException {
        if (supportsSensitiveDynamicProperties == null) {
            return;
        }
        writeBooleanElement("supportsSensitiveDynamicProperties", true);
    }

    @Override
    protected void writePrimaryNodeOnly(PrimaryNodeOnly primaryNodeOnly) throws IOException {
        if (primaryNodeOnly == null) {
            return;
        }
        writeBooleanElement("primaryNodeOnly", true);
    }

    @Override
    protected void writeSideEffectFree(SideEffectFree sideEffectFree) throws IOException {
        if (sideEffectFree == null) {
            return;
        }
        writeBooleanElement("sideEffectFree", true);
    }

    @Override
    protected void writeDefaultSchedule(DefaultSchedule defaultSchedule) throws IOException {
        if (defaultSchedule == null) {
            return;
        }

        writeStartElement("defaultSchedule");
        writeTextElement("strategy", defaultSchedule.strategy().name());
        writeTextElement("period", defaultSchedule.period());
        writeTextElement("concurrentTasks", String.valueOf(defaultSchedule.concurrentTasks()));
        writeEndElement();
    }

    @Override
    protected void writeDefaultSettings(DefaultSettings defaultSettings) throws IOException {
        if (defaultSettings == null) {
            return;
        }

        writeStartElement("defaultSettings");
        writeTextElement("yieldDuration", defaultSettings.yieldDuration());
        writeTextElement("penaltyDuration", defaultSettings.penaltyDuration());
        writeTextElement("bulletinLevel", defaultSettings.bulletinLevel().name());
        writeEndElement();
    }

    @Override
    protected void writeFooter(final ConfigurableComponent component) throws IOException {
        writeEndElement();
    }

    @Override
    protected void writeProvidedServices(final Collection<ServiceAPI> providedServices) throws IOException {
        if (providedServices == null || providedServices.isEmpty()) {
            return;
        }
        writeArray("providedServiceAPIs", providedServices, this::writeProvidedService);
    }

    private void writeProvidedService(final ServiceAPI service) throws IOException {
        writeStartElement("providedServiceAPI");

        writeTextElement("className", service.getClassName());
        writeTextElement("groupId", service.getGroupId());
        writeTextElement("artifactId", service.getArtifactId());
        writeTextElement("version", service.getVersion());

        writeEndElement();
    }

    private <T> void writeArray(final String tagName, final Collection<T> values, final ElementWriter<T> writer) throws IOException {
        writeStartElement(tagName);

        if (values != null) {
            for (final T value : values) {
                writer.write(value);
            }
        }

        writeEndElement();
    }


    private void writeTextArray(final String outerTagName, final String elementTagName, final Collection<String> values) throws IOException {
        writeTextArray(outerTagName, elementTagName, values, String::toString);
    }

    private <T> void writeTextArray(final String outerTagName, final String elementTagName, final Collection<T> values, final Function<T, String> transform) throws IOException {
        writeStartElement(outerTagName);

        if (values != null) {
            for (final T value : values) {
                writeStartElement(elementTagName);
                if (value != null) {
                    writeText(transform.apply(value));
                }
                writeEndElement();
            }
        }

        writeEndElement();
    }

    private void writeText(final String text) throws IOException {
        if (text == null) {
            return;
        }

        try {
            writer.writeCharacters(text);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeStartElement(final String elementName) throws IOException {
        try {
            writer.writeStartElement(elementName);
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeEndElement() throws IOException {
        try {
            writer.writeEndElement();
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private void writeTextElement(final String name, final String text) throws IOException {
        writeStartElement(name);
        writeText(text);
        writeEndElement();
    }

    private void writeBooleanElement(final String name, final boolean value) throws IOException {
        writeTextElement(name, String.valueOf(value));
    }

    private interface ElementWriter<T> {
        void write(T value) throws IOException;
    }
}
