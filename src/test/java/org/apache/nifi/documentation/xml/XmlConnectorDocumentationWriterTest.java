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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.CapabilityTag;
import org.apache.nifi.annotation.documentation.DeprecationNotice;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.ConfigVerificationResult;
import org.apache.nifi.components.connector.AbstractConnector;
import org.apache.nifi.components.connector.ConfigurationStep;
import org.apache.nifi.components.connector.Connector;
import org.apache.nifi.components.connector.ConnectorPropertyDescriptor;
import org.apache.nifi.components.connector.ConnectorPropertyGroup;
import org.apache.nifi.components.connector.PropertyType;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.documentation.ExtensionType;
import org.apache.nifi.flow.VersionedExternalFlow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class XmlConnectorDocumentationWriterTest {

    @Test
    void testWriteMinimalConnector() throws Exception {
        final Connector connector = new MinimalConnector();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);
    }

    @Test
    void testWriteConnectorWithDescription() throws Exception {
        final Connector connector = new DescribedConnector();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node descriptionNode = findNode("/extension/description", document);
        assertNotNull(descriptionNode);
        assertEquals("A connector with a description", descriptionNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithTags() throws Exception {
        final Connector connector = new TaggedConnector();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node tagsNode = findNode("/extension/tags", document);
        assertNotNull(tagsNode);

        final List<String> tagValues = new ArrayList<>();
        final NodeList tagNodes = tagsNode.getChildNodes();
        for (int i = 0; i < tagNodes.getLength(); i++) {
            final Node tagNode = tagNodes.item(i);
            assertEquals("tag", tagNode.getNodeName());
            tagValues.add(tagNode.getTextContent());
        }

        assertEquals(List.of("test", "documentation", "connector"), tagValues);
    }

    @Test
    void testWriteDeprecatedConnector() throws Exception {
        final Connector connector = new DeprecatedConnector();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node deprecationNoticeNode = findNode("/extension/deprecationNotice", document);
        assertNotNull(deprecationNoticeNode);

        final Node reasonNode = findNode("/extension/deprecationNotice/reason", document);
        assertNotNull(reasonNode);
        assertEquals("Use a different connector", reasonNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithSeeAlso() throws Exception {
        final Connector connector = new SeeAlsoConnector();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node seeAlsoNode = findNode("/extension/seeAlso", document);
        assertNotNull(seeAlsoNode);

        final List<String> seeAlsoValues = new ArrayList<>();
        final NodeList seeNodes = seeAlsoNode.getChildNodes();
        for (int i = 0; i < seeNodes.getLength(); i++) {
            final Node seeNode = seeNodes.item(i);
            assertEquals("see", seeNode.getNodeName());
            seeAlsoValues.add(seeNode.getTextContent());
        }

        assertEquals(1, seeAlsoValues.size());
        assertEquals("org.apache.nifi.documentation.xml.XmlConnectorDocumentationWriterTest$MinimalConnector", seeAlsoValues.getFirst());
    }

    @Test
    void testWriteConnectorWithConfigurationSteps() throws Exception {
        final Connector connector = new ConnectorWithConfigurationSteps();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node configStepsNode = findNode("/extension/configurationSteps", document);
        assertNotNull(configStepsNode);

        final Node firstStepNode = findNode("/extension/configurationSteps/configurationStep[name='Step One']", document);
        assertNotNull(firstStepNode);

        final Node stepNameNode = findNode("/extension/configurationSteps/configurationStep/name", document);
        assertNotNull(stepNameNode);
        assertEquals("Step One", stepNameNode.getTextContent());

        final Node stepDescriptionNode = findNode("/extension/configurationSteps/configurationStep/description", document);
        assertNotNull(stepDescriptionNode);
        assertEquals("First configuration step", stepDescriptionNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithConfigurationStepWithoutDescription() throws Exception {
        final Connector connector = new ConnectorWithStepWithoutDescription();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node stepNameNode = findNode("/extension/configurationSteps/configurationStep/name", document);
        assertNotNull(stepNameNode);
        assertEquals("Minimal Step", stepNameNode.getTextContent());

        // Step description is optional - should not be present when not provided
        final Node stepDescriptionNode = findNode("/extension/configurationSteps/configurationStep/description", document);
        assertNull(stepDescriptionNode);
    }

    @Test
    void testWriteConnectorWithPropertyGroups() throws Exception {
        final Connector connector = new ConnectorWithPropertyGroups();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node propertyGroupsNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups", document);
        assertNotNull(propertyGroupsNode);

        final Node propertyGroupNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup", document);
        assertNotNull(propertyGroupNode);

        final Node groupNameNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/name", document);
        assertNotNull(groupNameNode);
        assertEquals("Connection Settings", groupNameNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithProperties() throws Exception {
        final Connector connector = new ConnectorWithProperties();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node propertiesNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties", document);
        assertNotNull(propertiesNode);

        final Node propertyNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property", document);
        assertNotNull(propertyNode);

        final Node propertyNameNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/name", document);
        assertNotNull(propertyNameNode);
        assertEquals("Hostname", propertyNameNode.getTextContent());

        final Node propertyDescriptionNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/description", document);
        assertNotNull(propertyDescriptionNode);
        assertEquals("The hostname to connect to", propertyDescriptionNode.getTextContent());

        final Node propertyRequiredNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/required", document);
        assertNotNull(propertyRequiredNode);
        assertEquals("true", propertyRequiredNode.getTextContent());

        final Node propertyTypeNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/propertyType", document);
        assertNotNull(propertyTypeNode);
        assertEquals("STRING", propertyTypeNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithPropertyDefaultValue() throws Exception {
        final Connector connector = new ConnectorWithPropertyDefaultValue();
        final Document document = writeDocumentation(connector);

        final Node defaultValueNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/defaultValue", document);
        assertNotNull(defaultValueNode);
        assertEquals("localhost", defaultValueNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithAllowableValues() throws Exception {
        final Connector connector = new ConnectorWithAllowableValues();
        final Document document = writeDocumentation(connector);

        final Node allowableValuesNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/allowableValues", document);
        assertNotNull(allowableValuesNode);

        final List<String> allowableValues = new ArrayList<>();
        final NodeList valueNodes = allowableValuesNode.getChildNodes();
        for (int i = 0; i < valueNodes.getLength(); i++) {
            final Node valueNode = valueNodes.item(i);
            if ("allowableValue".equals(valueNode.getNodeName())) {
                final Node valueTextNode = findNode("value", valueNode);
                if (valueTextNode != null) {
                    allowableValues.add(valueTextNode.getTextContent());
                }
            }
        }

        assertEquals(List.of("TCP", "UDP"), allowableValues);
    }

    @Test
    void testWriteConnectorWithPropertyDependencies() throws Exception {
        final Connector connector = new ConnectorWithPropertyDependencies();
        final Document document = writeDocumentation(connector);

        final Node dependenciesNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property[name='Port']/dependencies", document);
        assertNotNull(dependenciesNode);

        final Node dependencyNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property[name='Port']/dependencies/dependency", document);
        assertNotNull(dependencyNode);

        final Node dependencyPropertyNameNode = findNode(
                "/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property[name='Port']/dependencies/dependency/propertyName",
                document);
        assertNotNull(dependencyPropertyNameNode);
        assertEquals("Hostname", dependencyPropertyNameNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithStepDependencies() throws Exception {
        final Connector connector = new ConnectorWithStepDependencies();
        final Document document = writeDocumentation(connector);

        final Node stepDependenciesNode = findNode("/extension/configurationSteps/configurationStep[name='Step Two']/stepDependencies", document);
        assertNotNull(stepDependenciesNode);

        final Node stepDependencyNode = findNode("/extension/configurationSteps/configurationStep[name='Step Two']/stepDependencies/stepDependency", document);
        assertNotNull(stepDependencyNode);

        final Node stepNameNode = findNode("/extension/configurationSteps/configurationStep[name='Step Two']/stepDependencies/stepDependency/stepName", document);
        assertNotNull(stepNameNode);
        assertEquals("Step One", stepNameNode.getTextContent());

        final Node propertyNameNode = findNode("/extension/configurationSteps/configurationStep[name='Step Two']/stepDependencies/stepDependency/propertyName", document);
        assertNotNull(propertyNameNode);
        assertEquals("Enable Advanced", propertyNameNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithFetchableAllowableValues() throws Exception {
        final Connector connector = new ConnectorWithFetchableAllowableValues();
        final Document document = writeDocumentation(connector);

        final Node allowableValuesFetchableNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/allowableValuesFetchable", document);
        assertNotNull(allowableValuesFetchableNode);
        assertEquals("true", allowableValuesFetchableNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithNoDescription() throws Exception {
        final Connector connector = new MinimalConnector();
        final Document document = writeDocumentation(connector);

        final Node descriptionNode = findNode("/extension/description", document);
        assertNull(descriptionNode);
    }

    @Test
    void testWriteConnectorWithNoTags() throws Exception {
        final Connector connector = new MinimalConnector();
        final Document document = writeDocumentation(connector);

        final Node tagsNode = findNode("/extension/tags", document);
        assertNull(tagsNode);
    }

    @Test
    void testWriteConnectorWithPropertyDependencyValues() throws Exception {
        final Connector connector = new ConnectorWithPropertyDependencyValues();
        final Document document = writeDocumentation(connector);

        final String xpath = "/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup"
            + "/properties/property[name='Advanced Setting']/dependencies/dependency/dependentValues";
        final Node dependentValuesNode = findNode(xpath, document);
        assertNotNull(dependentValuesNode);

        final List<String> dependentValues = new ArrayList<>();
        final NodeList valueNodes = dependentValuesNode.getChildNodes();
        for (int i = 0; i < valueNodes.getLength(); i++) {
            final Node valueNode = valueNodes.item(i);
            if ("dependentValue".equals(valueNode.getNodeName())) {
                dependentValues.add(valueNode.getTextContent());
            }
        }

        assertEquals(2, dependentValues.size());
        assertTrue(dependentValues.contains("advanced"));
        assertTrue(dependentValues.contains("expert"));
    }

    @Test
    void testWriteConnectorWithStepDependencyValues() throws Exception {
        final Connector connector = new ConnectorWithStepDependencyValues();
        final Document document = writeDocumentation(connector);

        final Node dependentValuesNode = findNode("/extension/configurationSteps/configurationStep[name='Step Two']/stepDependencies/stepDependency/dependentValues", document);
        assertNotNull(dependentValuesNode);

        final List<String> dependentValues = new ArrayList<>();
        final NodeList valueNodes = dependentValuesNode.getChildNodes();
        for (int i = 0; i < valueNodes.getLength(); i++) {
            final Node valueNode = valueNodes.item(i);
            if ("dependentValue".equals(valueNode.getNodeName())) {
                dependentValues.add(valueNode.getTextContent());
            }
        }

        assertEquals(1, dependentValues.size());
        assertEquals("true", dependentValues.getFirst());
    }

    @Test
    void testWriteConnectorWithMultipleConfigurationSteps() throws Exception {
        final Connector connector = new ConnectorWithMultipleSteps();
        final Document document = writeDocumentation(connector);

        final Node configStepsNode = findNode("/extension/configurationSteps", document);
        assertNotNull(configStepsNode);

        final List<String> stepNames = new ArrayList<>();
        final NodeList stepNodes = configStepsNode.getChildNodes();
        for (int i = 0; i < stepNodes.getLength(); i++) {
            final Node stepNode = stepNodes.item(i);
            if ("configurationStep".equals(stepNode.getNodeName())) {
                final Node nameNode = findNode("name", stepNode);
                if (nameNode != null) {
                    stepNames.add(nameNode.getTextContent());
                }
            }
        }

        assertEquals(List.of("Connection", "Authentication", "Advanced"), stepNames);
    }

    @Test
    void testWriteConnectorWithIntegerPropertyType() throws Exception {
        final Connector connector = new ConnectorWithIntegerProperty();
        final Document document = writeDocumentation(connector);

        final Node propertyTypeNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/propertyType", document);
        assertNotNull(propertyTypeNode);
        assertEquals("INTEGER", propertyTypeNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithBooleanPropertyType() throws Exception {
        final Connector connector = new ConnectorWithBooleanProperty();
        final Document document = writeDocumentation(connector);

        final Node propertyTypeNode = findNode("/extension/configurationSteps/configurationStep/propertyGroups/propertyGroup/properties/property/propertyType", document);
        assertNotNull(propertyTypeNode);
        assertEquals("BOOLEAN", propertyTypeNode.getTextContent());
    }

    @Test
    void testWriteConnectorWithCapabilityTags() throws Exception {
        final Connector connector = new CapabilityTagConnector();
        final Document document = writeDocumentation(connector);

        assertExtensionNameTypeFound(connector, ExtensionType.CONNECTOR, document);

        final Node capabilityTagsNode = findNode("/extension/capabilityTags", document);
        assertNotNull(capabilityTagsNode);

        final NodeList capabilityTagNodes = capabilityTagsNode.getChildNodes();
        assertEquals(2, capabilityTagNodes.getLength());

        final Node firstTag = capabilityTagNodes.item(0);
        assertEquals("capabilityTag", firstTag.getNodeName());
        final Node firstKey = firstTag.getFirstChild();
        assertEquals("key", firstKey.getNodeName());
        assertEquals("vendor", firstKey.getTextContent());
        final Node firstValue = firstKey.getNextSibling();
        assertEquals("value", firstValue.getNodeName());
        assertEquals("Acme Corp", firstValue.getTextContent());

        final Node secondTag = capabilityTagNodes.item(1);
        assertEquals("capabilityTag", secondTag.getNodeName());
        final Node secondKey = secondTag.getFirstChild();
        assertEquals("key", secondKey.getNodeName());
        assertEquals("category", secondKey.getTextContent());
        final Node secondValue = secondKey.getNextSibling();
        assertEquals("value", secondValue.getNodeName());
        assertEquals("networking", secondValue.getTextContent());
    }

    @Test
    void testWriteConnectorWithNoCapabilityTags() throws Exception {
        final Connector connector = new MinimalConnector();
        final Document document = writeDocumentation(connector);

        final Node capabilityTagsNode = findNode("/extension/capabilityTags", document);
        assertNull(capabilityTagsNode);
    }

    private Node findNode(final String expression, final Node node) throws XPathExpressionException {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath path = factory.newXPath();

        return path.evaluateExpression(expression, node, Node.class);
    }

    private void assertExtensionNameTypeFound(final Connector connector, final ExtensionType expectedExtensionType, final Document document) {
        assertNotNull(document);

        final Node extensionNode = document.getFirstChild();
        assertEquals("extension", extensionNode.getNodeName());

        final Node nameNode = extensionNode.getFirstChild();
        assertEquals("name", nameNode.getNodeName());
        assertEquals(connector.getClass().getName(), nameNode.getTextContent());

        final Node typeNode = nameNode.getNextSibling();
        assertEquals("type", typeNode.getNodeName());
        assertEquals(expectedExtensionType.name(), typeNode.getTextContent());
    }

    private Document writeDocumentation(final Connector connector) throws Exception {
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final DOMResult result = new DOMResult(document);
        final XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(result);

        final XmlConnectorDocumentationWriter documentationWriter = new XmlConnectorDocumentationWriter(streamWriter);

        try {
            documentationWriter.initialize(connector);
            documentationWriter.write(connector);
        } finally {
            streamWriter.close();
        }

        return document;
    }

    // Test Connector implementations

    private static class MinimalConnector extends AbstractConnector {
        @Override
        public VersionedExternalFlow getInitialFlow() {
            return null;
        }

        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            return Collections.emptyList();
        }

        @Override
        protected void onStepConfigured(final String stepName, final FlowContext workingContext) {
        }

        @Override
        public void applyUpdate(final FlowContext workingFlowContext, final FlowContext activeFlowContext) {
        }

        @Override
        public List<ConfigVerificationResult> verifyConfigurationStep(final String stepName, final Map<String, String> overrides, final FlowContext flowContext) {
            return Collections.emptyList();
        }
    }

    @CapabilityDescription("A connector with a description")
    private static class DescribedConnector extends MinimalConnector {
    }

    @Tags({"test", "documentation", "connector"})
    private static class TaggedConnector extends MinimalConnector {
    }

    @DeprecationNotice(reason = "Use a different connector")
    private static class DeprecatedConnector extends MinimalConnector {
    }

    @SeeAlso(classNames = "org.apache.nifi.documentation.xml.XmlConnectorDocumentationWriterTest$MinimalConnector")
    private static class SeeAlsoConnector extends MinimalConnector {
    }

    private static class ConnectorWithConfigurationSteps extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            return List.of(
                new ConfigurationStep.Builder()
                    .name("Step One")
                    .description("First configuration step")
                    .build()
            );
        }
    }

    private static class ConnectorWithStepWithoutDescription extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            return List.of(
                new ConfigurationStep.Builder()
                    .name("Minimal Step")
                    .build()
            );
        }
    }

    private static class ConnectorWithPropertyGroups extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Connection Settings")
                .description("Settings for connection")
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Connection Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithProperties extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
                .name("Hostname")
                .description("The hostname to connect to")
                .required(true)
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Connection Settings")
                .addProperty(property)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Connection Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithPropertyDefaultValue extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
                .name("Hostname")
                .description("The hostname to connect to")
                .defaultValue("localhost")
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Connection Settings")
                .addProperty(property)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Connection Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithAllowableValues extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
                .name("Protocol")
                .description("The protocol to use")
                .allowableValues(
                    new AllowableValue("TCP", "TCP", "Transmission Control Protocol"),
                    new AllowableValue("UDP", "UDP", "User Datagram Protocol")
                )
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Protocol Settings")
                .addProperty(property)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Protocol Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithPropertyDependencies extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor hostnameProperty = new ConnectorPropertyDescriptor.Builder()
                .name("Hostname")
                .description("The hostname to connect to")
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyDescriptor portProperty = new ConnectorPropertyDescriptor.Builder()
                .name("Port")
                .description("The port to connect to")
                .type(PropertyType.INTEGER)
                .dependsOn(hostnameProperty)
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Connection Settings")
                .addProperty(hostnameProperty)
                .addProperty(portProperty)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Connection Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithStepDependencies extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor enableAdvanced = new ConnectorPropertyDescriptor.Builder()
                .name("Enable Advanced")
                .description("Enable advanced settings")
                .type(PropertyType.BOOLEAN)
                .build();

            final ConnectorPropertyGroup group1 = ConnectorPropertyGroup.builder()
                .name("Basic Settings")
                .addProperty(enableAdvanced)
                .build();

            final ConfigurationStep step1 = new ConfigurationStep.Builder()
                .name("Step One")
                .propertyGroups(List.of(group1))
                .build();

            final ConnectorPropertyDescriptor advancedProperty = new ConnectorPropertyDescriptor.Builder()
                .name("Advanced Setting")
                .description("An advanced setting")
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyGroup group2 = ConnectorPropertyGroup.builder()
                .name("Advanced Settings")
                .addProperty(advancedProperty)
                .build();

            final ConfigurationStep step2 = new ConfigurationStep.Builder()
                .name("Step Two")
                .propertyGroups(List.of(group2))
                .dependsOn(step1, enableAdvanced, "true")
                .build();

            return List.of(step1, step2);
        }
    }

    private static class ConnectorWithFetchableAllowableValues extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
                .name("Dynamic Options")
                .description("Options that are fetched dynamically")
                .allowableValuesFetchable(true)
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Dynamic Settings")
                .addProperty(property)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Dynamic Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithPropertyDependencyValues extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor modeProperty = new ConnectorPropertyDescriptor.Builder()
                .name("Mode")
                .description("The mode of operation")
                .type(PropertyType.STRING)
                .allowableValues("basic", "advanced", "expert")
                .build();

            final ConnectorPropertyDescriptor advancedProperty = new ConnectorPropertyDescriptor.Builder()
                .name("Advanced Setting")
                .description("An advanced setting")
                .type(PropertyType.STRING)
                .dependsOn(modeProperty, "advanced", "expert")
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Settings")
                .addProperty(modeProperty)
                .addProperty(advancedProperty)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Settings Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithStepDependencyValues extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor enableAdvanced = new ConnectorPropertyDescriptor.Builder()
                .name("Enable Advanced")
                .description("Enable advanced settings")
                .type(PropertyType.BOOLEAN)
                .allowableValues("true", "false")
                .defaultValue("false")
                .build();

            final ConnectorPropertyGroup group1 = ConnectorPropertyGroup.builder()
                .name("Basic Settings")
                .addProperty(enableAdvanced)
                .build();

            final ConfigurationStep step1 = new ConfigurationStep.Builder()
                .name("Step One")
                .propertyGroups(List.of(group1))
                .build();

            final ConnectorPropertyDescriptor advancedProperty = new ConnectorPropertyDescriptor.Builder()
                .name("Advanced Setting")
                .description("An advanced setting")
                .type(PropertyType.STRING)
                .build();

            final ConnectorPropertyGroup group2 = ConnectorPropertyGroup.builder()
                .name("Advanced Settings")
                .addProperty(advancedProperty)
                .build();

            final ConfigurationStep step2 = new ConfigurationStep.Builder()
                .name("Step Two")
                .propertyGroups(List.of(group2))
                .dependsOn(step1, enableAdvanced, "true")
                .build();

            return List.of(step1, step2);
        }
    }

    private static class ConnectorWithMultipleSteps extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyGroup connectionGroup = ConnectorPropertyGroup.builder()
                .name("Connection")
                .build();

            final ConnectorPropertyGroup authGroup = ConnectorPropertyGroup.builder()
                .name("Auth")
                .build();

            final ConnectorPropertyGroup advancedGroup = ConnectorPropertyGroup.builder()
                .name("Options")
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Connection")
                    .description("Connection settings")
                    .propertyGroups(List.of(connectionGroup))
                    .build(),
                new ConfigurationStep.Builder()
                    .name("Authentication")
                    .description("Authentication settings")
                    .propertyGroups(List.of(authGroup))
                    .build(),
                new ConfigurationStep.Builder()
                    .name("Advanced")
                    .description("Advanced settings")
                    .propertyGroups(List.of(advancedGroup))
                    .build()
            );
        }
    }

    private static class ConnectorWithIntegerProperty extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
                .name("Port")
                .description("The port number")
                .type(PropertyType.INTEGER)
                .defaultValue("8080")
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Connection")
                .addProperty(property)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Connection Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    private static class ConnectorWithBooleanProperty extends MinimalConnector {
        @Override
        public List<ConfigurationStep> getConfigurationSteps() {
            final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
                .name("Enabled")
                .description("Whether the feature is enabled")
                .type(PropertyType.BOOLEAN)
                .defaultValue("true")
                .build();

            final ConnectorPropertyGroup group = ConnectorPropertyGroup.builder()
                .name("Settings")
                .addProperty(property)
                .build();

            return List.of(
                new ConfigurationStep.Builder()
                    .name("Settings Step")
                    .propertyGroups(List.of(group))
                    .build()
            );
        }
    }

    @CapabilityTag(key = "vendor", value = "Acme Corp")
    @CapabilityTag(key = "category", value = "networking")
    private static class CapabilityTagConnector extends MinimalConnector {
    }
}

