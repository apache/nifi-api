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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.nifi.annotation.documentation.CapabilityTag;
import org.apache.nifi.annotation.documentation.DeprecationNotice;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.components.DescribedValue;
import org.apache.nifi.components.connector.ConfigurationStep;
import org.apache.nifi.components.connector.ConfigurationStepDependency;
import org.apache.nifi.components.connector.Connector;
import org.apache.nifi.components.connector.ConnectorPropertyDependency;
import org.apache.nifi.components.connector.ConnectorPropertyDescriptor;
import org.apache.nifi.components.connector.ConnectorPropertyGroup;
import org.apache.nifi.documentation.AbstractConnectorDocumentationWriter;
import org.apache.nifi.documentation.ExtensionType;

/**
 * XML-based implementation of ConnectorDocumentationWriter.
 * Please note that while this class lives within the nifi-api, it is provided primarily as a means for documentation components within
 * the NiFi NAR Maven Plugin. Its home is the nifi-api, however, because the API is needed in order to extract the relevant information and
 * the NAR Maven Plugin cannot have a direct dependency on nifi-api (doing so would cause a circular dependency). By having this homed within
 * the nifi-api, the Maven plugin is able to discover the class dynamically and invoke the one or two methods necessary to create the documentation.
 *
 * This is a new capability for Connectors and therefore, you should
 * <b>NOTE WELL:</b> At this time, while this class is part of nifi-api, it is still evolving and may change in a non-backward-compatible manner or even be
 * removed from one incremental release to the next. Use at your own risk!
 */
public class XmlConnectorDocumentationWriter extends AbstractConnectorDocumentationWriter {
    private final XMLStreamWriter writer;

    public XmlConnectorDocumentationWriter(final OutputStream out) throws XMLStreamException {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
    }

    public XmlConnectorDocumentationWriter(final XMLStreamWriter writer) {
        this.writer = writer;
    }

    @Override
    protected void writeHeader(final Connector connector) throws IOException {
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

        final Class<?>[] classes = deprecationNotice.alternatives();
        final String[] classNames = deprecationNotice.classNames();

        final Set<String> alternatives = new LinkedHashSet<>();
        for (final Class<?> alternativeClass : classes) {
            alternatives.add(alternativeClass.getName());
        }

        Collections.addAll(alternatives, classNames);

        writeStartElement("deprecationNotice");
        writeTextElement("reason", deprecationNotice.reason());
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
        if (tags == null || tags.isEmpty()) {
            return;
        }
        writeTextArray("tags", "tag", tags);
    }

    @Override
    protected void writeCapabilityTags(final List<CapabilityTag> capabilityTags) throws IOException {
        if (capabilityTags == null || capabilityTags.isEmpty()) {
            return;
        }
        writeStartElement("capabilityTags");
        for (final CapabilityTag capabilityTag : capabilityTags) {
            writeStartElement("capabilityTag");
            writeTextElement("key", capabilityTag.key());
            writeTextElement("value", capabilityTag.value());
            writeEndElement();
        }
        writeEndElement();
    }

    @Override
    protected void writeConfigurationSteps(final List<ConfigurationStep> configurationSteps) throws IOException {
        if (configurationSteps == null || configurationSteps.isEmpty()) {
            return;
        }

        writeStartElement("configurationSteps");
        for (final ConfigurationStep step : configurationSteps) {
            writeConfigurationStep(step);
        }
        writeEndElement();
    }

    private void writeConfigurationStep(final ConfigurationStep step) throws IOException {
        writeStartElement("configurationStep");

        writeTextElement("name", step.getName());
        if (step.getDescription() != null) {
            writeTextElement("description", step.getDescription());
        }

        // Write step dependencies
        final Set<ConfigurationStepDependency> stepDependencies = step.getDependencies();
        if (stepDependencies != null && !stepDependencies.isEmpty()) {
            writeStartElement("stepDependencies");
            for (final ConfigurationStepDependency dependency : stepDependencies) {
                writeConfigurationStepDependency(dependency);
            }
            writeEndElement();
        }

        // Write property groups
        final List<ConnectorPropertyGroup> propertyGroups = step.getPropertyGroups();
        if (propertyGroups != null && !propertyGroups.isEmpty()) {
            writeStartElement("propertyGroups");
            for (final ConnectorPropertyGroup group : propertyGroups) {
                writePropertyGroup(group);
            }
            writeEndElement();
        }

        writeEndElement();
    }

    private void writeConfigurationStepDependency(final ConfigurationStepDependency dependency) throws IOException {
        writeStartElement("stepDependency");

        writeTextElement("stepName", dependency.getStepName());
        writeTextElement("propertyName", dependency.getPropertyName());

        final Set<String> dependentValues = dependency.getDependentValues();
        if (dependentValues != null && !dependentValues.isEmpty()) {
            writeTextArray("dependentValues", "dependentValue", dependentValues);
        }

        writeEndElement();
    }

    private void writePropertyGroup(final ConnectorPropertyGroup group) throws IOException {
        writeStartElement("propertyGroup");

        if (group.getName() != null) {
            writeTextElement("name", group.getName());
        }
        if (group.getDescription() != null) {
            writeTextElement("description", group.getDescription());
        }

        final List<ConnectorPropertyDescriptor> properties = group.getProperties();
        if (properties != null && !properties.isEmpty()) {
            writeStartElement("properties");
            for (final ConnectorPropertyDescriptor property : properties) {
                writeConnectorProperty(property);
            }
            writeEndElement();
        }

        writeEndElement();
    }

    private void writeConnectorProperty(final ConnectorPropertyDescriptor property) throws IOException {
        writeStartElement("property");

        writeTextElement("name", property.getName());
        if (property.getDescription() != null) {
            writeTextElement("description", property.getDescription());
        }
        if (property.getDefaultValue() != null) {
            writeTextElement("defaultValue", property.getDefaultValue());
        }
        writeBooleanElement("required", property.isRequired());
        writeTextElement("propertyType", property.getType().name());
        writeBooleanElement("allowableValuesFetchable", property.isAllowableValuesFetchable());

        // Write allowable values
        final List<DescribedValue> allowableValues = property.getAllowableValues();
        if (allowableValues != null && !allowableValues.isEmpty()) {
            writeStartElement("allowableValues");
            for (final DescribedValue value : allowableValues) {
                writeAllowableValue(value);
            }
            writeEndElement();
        }

        // Write property dependencies
        final Set<ConnectorPropertyDependency> dependencies = property.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            writeStartElement("dependencies");
            for (final ConnectorPropertyDependency dependency : dependencies) {
                writePropertyDependency(dependency);
            }
            writeEndElement();
        }

        writeEndElement();
    }

    private void writeAllowableValue(final DescribedValue value) throws IOException {
        writeStartElement("allowableValue");
        writeTextElement("displayName", value.getDisplayName());
        writeTextElement("value", value.getValue());
        if (value.getDescription() != null) {
            writeTextElement("description", value.getDescription());
        }
        writeEndElement();
    }

    private void writePropertyDependency(final ConnectorPropertyDependency dependency) throws IOException {
        writeStartElement("dependency");
        writeTextElement("propertyName", dependency.getPropertyName());

        final Set<String> dependentValues = dependency.getDependentValues();
        if (dependentValues != null && !dependentValues.isEmpty()) {
            writeTextArray("dependentValues", "dependentValue", dependentValues);
        }

        writeEndElement();
    }

    @Override
    protected void writeSeeAlso(final SeeAlso seeAlso) throws IOException {
        if (seeAlso == null) {
            return;
        }

        final Class<?>[] classes = seeAlso.value();
        final String[] classNames = seeAlso.classNames();

        final Set<String> toSee = new LinkedHashSet<>();
        if (classes != null) {
            for (final Class<?> classToSee : classes) {
                toSee.add(classToSee.getName());
            }
        }

        if (classNames != null) {
            Collections.addAll(toSee, classNames);
        }

        writeTextArray("seeAlso", "see", toSee);
    }

    @Override
    protected void writeFooter(final Connector connector) throws IOException {
        writeEndElement();
    }

    // Utility methods for XML writing

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

    private void writeText(final String text) throws IOException {
        if (text == null) {
            return;
        }
        try {
            writer.writeCharacters(text);
        } catch (final XMLStreamException e) {
            throw new IOException(e);
        }
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

}

