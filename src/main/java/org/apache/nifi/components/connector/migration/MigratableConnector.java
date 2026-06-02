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
import org.apache.nifi.flow.VersionedComponentState;

/**
 * Optional {@link Connector} capability for migration from a source flow.
 *
 * <p>
 * Migration runs in two phases:
 * </p>
 * <ol>
 *     <li>{@link #migrateConfiguration(ConnectorMigrationContext)} records configuration changes and copied assets.</li>
 *     <li>{@link #migrateState(ConnectorMigrationContext)} records component {@link VersionedComponentState}.</li>
 * </ol>
 *
 * <p>
 * Connectors must not call
 * {@link ConnectorInitializationContext#updateFlow(FlowContext, org.apache.nifi.flow.VersionedExternalFlow)
 * updateFlow(...)} during migration. The framework applies recorded configuration by calling
 * {@link Connector#applyUpdate(FlowContext, FlowContext) applyUpdate(...)} between phases, then writes recorded
 * state. If any phase fails, migration is rolled back.
 * </p>
 *
 * <p>
 * Sensitive values are not included in the source flow and must be configured by the user after migration.
 * </p>
 *
 * <p>
 * <b>Implementation Note:</b> This API is experimental and may change between releases.
 * </p>
 */
public interface MigratableConnector {

    /**
     * Returns whether this Connector supports migration from the source flow in the given context.
     * This method is read-only and must not call context write methods.
     *
     * @param context migration context
     * @return {@code true} when migration is supported
     */
    boolean isMigrationSupported(ConnectorMigrationContext context);

    /**
     * First migration phase. Record configuration changes using
     * {@link ConnectorMigrationContext#setProperties(String, java.util.Map)} or
     * {@link ConnectorMigrationContext#replaceProperties(String, java.util.Map)}, and copy assets as needed.
     * The framework calls {@link Connector#applyUpdate(FlowContext, FlowContext)} after this method returns.
     *
     * @param context migration context
     * @throws FlowUpdateException when migration fails
     */
    void migrateConfiguration(ConnectorMigrationContext context) throws FlowUpdateException;

    /**
     * Second migration phase. This method is invoked after the framework rebuilds the managed flow from configuration.
     * Record component state using {@link ConnectorMigrationContext#setComponentState(String, VersionedComponentState)}.
     *
     * @param context migration context
     * @throws FlowUpdateException when state migration fails
     */
    void migrateState(ConnectorMigrationContext context) throws FlowUpdateException;
}
