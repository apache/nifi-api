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

import org.apache.nifi.annotation.documentation.DeprecationNotice;
import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.resource.ResourceCardinality;
import org.apache.nifi.components.resource.ResourceType;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.documentation.ExtensionDocumentationWriter;
import org.apache.nifi.documentation.ExtensionType;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class XmlDocumentationWriterTest {

    private static final Relationship SUCCESS_RELATIONSHIP = new Relationship.Builder()
            .name("success")
            .description("FlowFile processed without errors")
            .build();

    private static final Relationship FAILURE_RELATIONSHIP = new Relationship.Builder()
            .name("failure")
            .description("FlowFile processed with errors")
            .build();

    private static final Relationship ORIGINAL_RELATIONSHIP = new Relationship.Builder()
            .name("original")
            .description("FlowFile processed without changes")
            .build();

    private static final Set<Relationship> UNORDERED_RELATIONSHIPS = Set.of(
            SUCCESS_RELATIONSHIP,
            FAILURE_RELATIONSHIP,
            ORIGINAL_RELATIONSHIP
    );

    private static final List<String> EXPECTED_RELATIONSHIP_NAMES = List.of(
            FAILURE_RELATIONSHIP.getName(),
            ORIGINAL_RELATIONSHIP.getName(),
            SUCCESS_RELATIONSHIP.getName()
    );

    private static final String FIRST_DEPENDENT_VALUE = "First";

    private static final String SECOND_DEPENDENT_VALUE = "Second";

    private static final String THIRD_DEPENDENT_VALUE = "Third";

    private static final PropertyDescriptor FIRST_PROPERTY = new PropertyDescriptor.Builder()
            .name("First Property")
            .build();

    private static final PropertyDescriptor SECOND_PROPERTY = new PropertyDescriptor.Builder()
            .name("Second Property")
            .identifiesExternalResource(ResourceCardinality.SINGLE, ResourceType.URL, ResourceType.TEXT, ResourceType.FILE)
            .dependsOn(FIRST_PROPERTY, THIRD_DEPENDENT_VALUE, SECOND_DEPENDENT_VALUE, FIRST_DEPENDENT_VALUE)
            .build();

    private static final PropertyDescriptor THIRD_PROPERTY = new PropertyDescriptor.Builder()
            .name("Third Property")
            .dependsOn(SECOND_PROPERTY)
            .dependsOn(FIRST_PROPERTY)
            .build();

    private static final List<PropertyDescriptor> PROPERTY_DESCRIPTORS = List.of(
            FIRST_PROPERTY,
            SECOND_PROPERTY,
            THIRD_PROPERTY
    );

    private static final List<String> EXPECTED_PROPERTY_NAMES = List.of(
            FIRST_PROPERTY.getName(),
            SECOND_PROPERTY.getName(),
            THIRD_PROPERTY.getName()
    );

    private static final List<String> EXPECTED_DEPENDENT_PROPERTY_NAMES = List.of(
            FIRST_PROPERTY.getName(),
            SECOND_PROPERTY.getName()
    );


    private static final List<String> EXPECTED_DEPENDENT_VALUES = List.of(
            FIRST_DEPENDENT_VALUE,
            SECOND_DEPENDENT_VALUE,
            THIRD_DEPENDENT_VALUE
    );

    private static final List<String> EXPECTED_RESOURCE_TYPE_NAMES = List.of(
            ResourceType.FILE.name(),
            ResourceType.TEXT.name(),
            ResourceType.URL.name()
    );

    @Test
    void testWriteMinimalProcessor() throws Exception {
        final Processor processor = new MinimalProcessor();
        final Document document = writeDocumentation(processor);

        assertExtensionNameTypeFound(processor, ExtensionType.PROCESSOR, document);
    }

    @Test
    void testWriteMinimalControllerService() throws Exception {
        final ControllerService controllerService = new MinimalControllerService();
        final Document document = writeDocumentation(controllerService);

        assertExtensionNameTypeFound(controllerService, ExtensionType.CONTROLLER_SERVICE, document);
    }

    @Test
    void testWriteDeprecatedControllerService() throws Exception {
        final ControllerService controllerService = new DeprecatedControllerService();
        final Document document = writeDocumentation(controllerService);

        assertExtensionNameTypeFound(controllerService, ExtensionType.CONTROLLER_SERVICE, document);

        final Node deprecationNoticeReason = findNode("/extension/deprecationNotice/reason", document);
        assertNotNull(deprecationNoticeReason);
        final Node reasonChildNode = deprecationNoticeReason.getFirstChild();
        assertNull(reasonChildNode.getFirstChild());

        final Node deprecationNoticeAlternatives = findNode("/extension/deprecationNotice/alternatives", document);
        assertNotNull(deprecationNoticeAlternatives);

        assertNull(deprecationNoticeAlternatives.getFirstChild());
    }

    @Test
    void testWriteRelationships() throws Exception {
        final Processor processor = new RelationshipProcessor();
        final Document document = writeDocumentation(processor);

        assertExtensionNameTypeFound(processor, ExtensionType.PROCESSOR, document);
        assertRelationshipsMatched(document);
    }

    @Test
    void testWritePropertyDescriptors() throws Exception {
        final Processor processor = new PropertyDescriptorProcessor();
        final Document document = writeDocumentation(processor);

        assertExtensionNameTypeFound(processor, ExtensionType.PROCESSOR, document);
        assertPropertyDescriptorsMatched(document);
        assertDependentPropertyNamesMatched(document);
        assertDependentPropertyValuesMatched(document);
        assertResourceTypesMatched(document);
    }

    private void assertRelationshipsMatched(final Document document) throws XPathExpressionException {
        final Node relationshipsNode = findNode("/extension/relationships", document);
        assertNotNull(relationshipsNode);

        final List<String> relationshipNames = new ArrayList<>();
        final NodeList relationships = relationshipsNode.getChildNodes();
        for (int i = 0; i < relationships.getLength(); i++) {
            final Node relationshipNode = relationships.item(i);
            assertEquals("relationship", relationshipNode.getNodeName());

            final Node relationshipNameNode = relationshipNode.getFirstChild();
            assertEquals("name", relationshipNameNode.getNodeName());

            final String relationshipName = relationshipNameNode.getTextContent();
            relationshipNames.add(relationshipName);
        }

        assertEquals(EXPECTED_RELATIONSHIP_NAMES, relationshipNames);
    }

    private void assertPropertyDescriptorsMatched(final Document document) throws XPathExpressionException {
        final Node propertiesNode = findNode("/extension/properties", document);
        assertNotNull(propertiesNode);

        final List<String> propertyNames = new ArrayList<>();
        final NodeList properties = propertiesNode.getChildNodes();
        for (int i = 0; i < properties.getLength(); i++) {
            final Node propertyNode = properties.item(i);
            assertEquals("property", propertyNode.getNodeName());

            final Node propertyNameNode = propertyNode.getFirstChild();
            assertEquals("name", propertyNameNode.getNodeName());

            final String propertyName = propertyNameNode.getTextContent();
            propertyNames.add(propertyName);
        }

        assertEquals(EXPECTED_PROPERTY_NAMES, propertyNames);
    }

    private void assertDependentPropertyNamesMatched(final Document document) throws XPathExpressionException {
        final Node dependenciesNode = findNode("/extension/properties/property[name='Third Property']/dependencies", document);
        assertNotNull(dependenciesNode);

        final NodeList dependencies = dependenciesNode.getChildNodes();
        final List<String> dependentPropertyNames = new ArrayList<>();
        for (int i = 0; i < dependencies.getLength(); i++) {
            final Node dependencyNode = dependencies.item(i);
            assertEquals("dependency", dependencyNode.getNodeName());

            final Node propertyNode = dependencyNode.getFirstChild();
            assertEquals("propertyName", propertyNode.getNodeName());

            final String propertyName = propertyNode.getTextContent();
            dependentPropertyNames.add(propertyName);
        }

        assertEquals(EXPECTED_DEPENDENT_PROPERTY_NAMES, dependentPropertyNames);
    }

    private void assertDependentPropertyValuesMatched(final Document document) throws XPathExpressionException {
        final Node dependentValuesNode = findNode("/extension/properties/property[name='Second Property']/dependencies/dependency/dependentValues", document);
        assertNotNull(dependentValuesNode);

        final NodeList dependentValues = dependentValuesNode.getChildNodes();
        final List<String> values = new ArrayList<>();
        for (int i = 0; i < dependentValues.getLength(); i++) {
            final Node valueNode = dependentValues.item(i);
            final String value = valueNode.getTextContent();
            values.add(value);
        }

        assertEquals(EXPECTED_DEPENDENT_VALUES, values);
    }

    private void assertResourceTypesMatched(final Document document) throws XPathExpressionException {
        final Node resourceTypesNode = findNode("/extension/properties/property[name='Second Property']/resourceDefinition/resourceTypes", document);
        assertNotNull(resourceTypesNode);

        final NodeList resourceTypes = resourceTypesNode.getChildNodes();
        final List<String> resourceTypeNames = new ArrayList<>();
        for (int i = 0; i < resourceTypes.getLength(); i++) {
            final Node resourceTypeNode = resourceTypes.item(i);
            assertEquals("resourceType", resourceTypeNode.getNodeName());
            final String resourceType = resourceTypeNode.getTextContent();
            resourceTypeNames.add(resourceType);
        }

        assertEquals(EXPECTED_RESOURCE_TYPE_NAMES, resourceTypeNames);
    }


    private Node findNode(final String expression, final Node node) throws XPathExpressionException {
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath path = factory.newXPath();

        return path.evaluateExpression(expression, node, Node.class);
    }

    private void assertExtensionNameTypeFound(final ConfigurableComponent component, final ExtensionType expectedExtensionType, final Document document) {
        assertNotNull(document);

        final Node extensionNode = document.getFirstChild();
        assertEquals("extension", extensionNode.getNodeName());

        final Node nameNode = extensionNode.getFirstChild();
        assertEquals("name", nameNode.getNodeName());
        assertEquals(component.getClass().getName(), nameNode.getTextContent());

        final Node typeNode = nameNode.getNextSibling();
        assertEquals("type", typeNode.getNodeName());
        assertEquals(expectedExtensionType.name(), typeNode.getTextContent());
    }

    private Document writeDocumentation(final ConfigurableComponent component) throws Exception {
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        final DOMResult result = new DOMResult(document);
        final XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(result);

        final ExtensionDocumentationWriter documentationWriter = new XmlDocumentationWriter(streamWriter);

        try {
            documentationWriter.write(component);
        } finally {
            streamWriter.close();
        }

        return document;
    }

    private static class MinimalProcessor extends AbstractProcessor {

        @Override
        public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        }
    }

    private static class MinimalControllerService extends AbstractControllerService {

    }

    @DeprecationNotice
    private static class DeprecatedControllerService extends AbstractControllerService {

    }

    private static class RelationshipProcessor extends AbstractProcessor {

        @Override
        public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        }

        @Override
        public Set<Relationship> getRelationships() {
            return UNORDERED_RELATIONSHIPS;
        }
    }

    private static class PropertyDescriptorProcessor extends AbstractProcessor {

        @Override
        public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        }

        @Override
        protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
            return PROPERTY_DESCRIPTORS;
        }
    }
}
