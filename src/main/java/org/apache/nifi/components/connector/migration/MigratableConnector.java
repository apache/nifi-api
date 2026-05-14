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

package org.apache.nifi.components.connector.migration;

import org.apache.nifi.components.connector.Connector;
import org.apache.nifi.components.connector.ConnectorInitializationContext;
import org.apache.nifi.components.connector.FlowUpdateException;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.flow.VersionedExternalFlow;

/**
 * <p>
 * An optional capability interface that may be implemented by a {@link Connector} to indicate that it supports
 * being populated from an existing source flow (for example, a Versioned Process Group already running on this
 * NiFi instance, or an uploaded flow definition). The framework discovers this capability by checking whether a
 * Connector is an instance of {@code MigratableConnector}; Connectors that do not implement this interface are
 * never offered as migration targets.
 * </p>
 *
 * <b>Implementation Note:</b> This API is currently experimental, as it is under very active development. As such,
 * it is subject to change without notice between releases.
 */
public interface MigratableConnector {

    /**
     * Indicates whether this Connector can be migrated from the source flow described by the given context.
     *
     * <p>
     * Implementations should inspect the source flow structure and metadata using
     * {@link ConnectorMigrationContext#getSourceFlow()} and return quickly without mutating the Connector or the
     * source flow. This method must not call {@link ConnectorMigrationContext#copyAssetFromSource(String)}.
     * </p>
     *
     * @param context the migration context describing the source flow and target Connector
     * @return {@code true} when this Connector can be migrated from the provided source flow
     */
    boolean isMigrationSupported(ConnectorMigrationContext context);

    /**
     * Migrates this Connector by updating its own managed flow to mirror the configuration, parameters, and component
     * state captured in the provided source flow. The source flow is a reference: it is read, not modified, and is not
     * installed onto the Connector. The Connector remains the owner of its flow and is responsible for translating the
     * source into its own representation.
     *
     * <p>
     * The framework guarantees the following preconditions when this method is invoked:
     * </p>
     * <ul>
     *     <li>The Connector is stopped.</li>
     *     <li>The Connector has not had any configuration changes applied by the user and has not been started.</li>
     * </ul>
     *
     * <p>
     * Because of these preconditions, the implementation updates the active {@link FlowContext} directly rather than
     * making use of {@code prepareForUpdate} and {@code applyUpdate}. Those two lifecycle methods exist to safely
     * transition a running Connector from one active configuration to another; for migration, the Connector is
     * already required to be in the target-safe state, so the working-to-active swap is unnecessary.
     * </p>
     *
     * <p>
     * Implementations are responsible for transforming the source flow, updating the active {@link FlowContext}, and
     * applying any parameter or step configuration changes needed by the Connector. Sensitive parameter values are not
     * present in the source flow and must be left for the user to configure after the migration completes.
     * </p>
     *
     * <p>
     * Connectors that extend {@code AbstractConnector} can typically retain their {@link ConnectorInitializationContext}
     * from {@code initialize(ConnectorInitializationContext)} and call
     * {@link ConnectorInitializationContext#updateFlow(FlowContext, VersionedExternalFlow)} using
     * {@link ConnectorMigrationContext#getActiveFlowContext()}.
     * </p>
     *
     * @param context the migration context describing the source flow and target Connector
     * @throws FlowUpdateException when the migration cannot be completed successfully
     */
    void migrate(ConnectorMigrationContext context) throws FlowUpdateException;
}
