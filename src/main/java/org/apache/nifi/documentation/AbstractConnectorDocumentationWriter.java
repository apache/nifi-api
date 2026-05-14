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

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.CapabilityTag;
import org.apache.nifi.annotation.documentation.DeprecationNotice;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.connector.ConfigurationStep;
import org.apache.nifi.components.connector.Connector;
import org.apache.nifi.documentation.init.DocumentationConnectorInitializationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for ConnectorDocumentationWriter that simplifies iterating over all information for a Connector,
 * creating a separate method for each piece of documentation, to ensure that implementations properly override
 * all methods and therefore properly account for all information about a Connector.
 *
 * Please note that while this class lives within the nifi-api, it is provided primarily as a means for documentation components within
 * the NiFi NAR Maven Plugin. Its home is the nifi-api, however, because the API is needed in order to extract the relevant information and
 * the NAR Maven Plugin cannot have a direct dependency on nifi-api (doing so would cause a circular dependency). By having this homed within
 * the nifi-api, the Maven plugin is able to discover the class dynamically and invoke the one or two methods necessary to create the documentation.
 *
 * This is a new capability for Connectors and therefore, you should
 * <b>NOTE WELL:</b> At this time, while this class is part of nifi-api, it is still evolving and may change in a non-backward-compatible manner or even be
 * removed from one incremental release to the next. Use at your own risk!
 */
public abstract class AbstractConnectorDocumentationWriter implements ConnectorDocumentationWriter {

    @Override
    public void initialize(final Connector connector) {
        connector.initialize(new DocumentationConnectorInitializationContext());
    }

    @Override
    public final void write(final Connector connector) throws IOException {
        writeHeader(connector);
        writeBody(connector);
        writeFooter(connector);
    }

    protected void writeBody(final Connector connector) throws IOException {
        writeExtensionName(connector.getClass().getName());
        writeExtensionType(ExtensionType.CONNECTOR);
        writeDeprecationNotice(connector.getClass().getAnnotation(DeprecationNotice.class));
        writeDescription(getDescription(connector));
        writeTags(getTags(connector));
        writeCapabilityTags(getCapabilityTags(connector));
        writeConfigurationSteps(connector.getConfigurationSteps());
        writeSeeAlso(connector.getClass().getAnnotation(SeeAlso.class));
    }

    protected String getDescription(final Connector connector) {
        final CapabilityDescription capabilityDescription = connector.getClass().getAnnotation(CapabilityDescription.class);
        if (capabilityDescription == null) {
            return null;
        }
        return capabilityDescription.value();
    }

    protected List<String> getTags(final Connector connector) {
        final Tags tags = connector.getClass().getAnnotation(Tags.class);
        if (tags == null) {
            return Collections.emptyList();
        }
        final String[] tagValues = tags.value();
        return tagValues == null ? Collections.emptyList() : Arrays.asList(tagValues);
    }

    protected List<CapabilityTag> getCapabilityTags(final Connector connector) {
        final CapabilityTag[] capabilityTags = connector.getClass().getAnnotationsByType(CapabilityTag.class);
        if (capabilityTags.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(capabilityTags);
    }

    protected abstract void writeHeader(Connector connector) throws IOException;

    protected abstract void writeExtensionName(String extensionName) throws IOException;

    protected abstract void writeExtensionType(ExtensionType extensionType) throws IOException;

    protected abstract void writeDeprecationNotice(DeprecationNotice deprecationNotice) throws IOException;

    protected abstract void writeDescription(String description) throws IOException;

    protected abstract void writeTags(List<String> tags) throws IOException;

    protected abstract void writeCapabilityTags(List<CapabilityTag> capabilityTags) throws IOException;

    protected abstract void writeConfigurationSteps(List<ConfigurationStep> configurationSteps) throws IOException;

    protected abstract void writeSeeAlso(SeeAlso seeAlso) throws IOException;

    protected abstract void writeFooter(Connector connector) throws IOException;

}

