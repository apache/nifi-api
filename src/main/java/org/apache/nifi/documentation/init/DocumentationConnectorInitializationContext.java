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
package org.apache.nifi.documentation.init;

import org.apache.nifi.components.connector.BundleCompatibility;
import org.apache.nifi.components.connector.ComponentBundleLookup;
import org.apache.nifi.components.connector.ConnectorInitializationContext;
import org.apache.nifi.components.connector.FlowUpdateException;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.flow.Bundle;
import org.apache.nifi.flow.VersionedExternalFlow;
import org.apache.nifi.logging.ComponentLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A ConnectorInitializationContext implementation for use during documentation generation.
 * This context provides minimal functionality needed to initialize a Connector for the purposes
 * of extracting its configuration steps and other documentation metadata.
 */
public class DocumentationConnectorInitializationContext implements ConnectorInitializationContext {
    private final String uuid = UUID.randomUUID().toString();

    @Override
    public String getIdentifier() {
        return uuid;
    }

    @Override
    public String getName() {
        return "DocumentationConnector";
    }

    @Override
    public ComponentLog getLogger() {
        return new NopComponentLog();
    }

    @Override
    public ComponentBundleLookup getComponentBundleLookup() {
        return new ComponentBundleLookup() {
            @Override
            public List<Bundle> getAvailableBundles(final String componentType) {
                return List.of();
            }

            @Override
            public Optional<Bundle> getLatestBundle(final String componentType) {
                return Optional.empty();
            }
        };
    }

    @Override
    public void updateFlow(final FlowContext flowContext, final VersionedExternalFlow versionedExternalFlow,
                           final BundleCompatibility bundleCompatability) throws FlowUpdateException {
        // No-op for documentation purposes - we don't actually update any flows
    }
}

