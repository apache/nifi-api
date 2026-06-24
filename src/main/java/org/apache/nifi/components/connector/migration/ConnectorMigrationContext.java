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

import org.apache.nifi.components.connector.AssetReference;
import org.apache.nifi.components.connector.ConfigurationStep;
import org.apache.nifi.components.connector.ConnectorInitializationContext;
import org.apache.nifi.components.connector.ConnectorValueReference;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.flow.VersionedComponentState;
import org.apache.nifi.flow.VersionedExternalFlow;

import java.util.Map;

/**
 * Migration context for a {@link MigratableConnector}.
 *
 * <p>
 * The source flow is read-only and is never installed directly. Write methods are phase-scoped:
 * {@link #setProperties(String, Map)} and {@link #replaceProperties(String, Map)} are valid only during
 * {@link MigratableConnector#migrateConfiguration(ConnectorMigrationContext)}, and
 * {@link #setComponentState(String, VersionedComponentState)} is valid only during
 * {@link MigratableConnector#migrateState(ConnectorMigrationContext)}.
 * </p>
 *
 * <p>
 * Calling {@link ConnectorInitializationContext#updateFlow(FlowContext, VersionedExternalFlow) updateFlow(...)}
 * through this context throws.
 * </p>
 */
public interface ConnectorMigrationContext {

    /**
     * Returns the source flow whose configuration, parameters, and component state the Connector should mirror into
     * its own managed flow.
     *
     * @return the source flow
     */
    VersionedExternalFlow getSourceFlow();

    /**
     * Indicates whether the migration source is a local Versioned Process Group on this NiFi instance.
     *
     * @return {@code true} when the source is local
     */
    boolean isLocalMigration();

    /**
     * Returns the active flow context for the Connector being migrated.
     * Calling {@code updateFlow(...)} through this context throws.
     *
     * @return active flow context
     */
    FlowContext getActiveFlowContext();

    /**
     * Copies the referenced source asset into the Connector asset namespace.
     *
     * <p>
     * When the source asset cannot be located in the source asset manager (for example because it was deleted from
     * the local source after the migration request started), the framework logs a warning and returns an
     * {@link AssetReference} whose {@link AssetReference#getAssetIdentifiers()} returns an empty set, so the
     * Connector can continue the migration without the missing asset. An empty asset reference is a normal,
     * expected return value and is the explicit signal that the asset was not migrated; callers should detect this
     * by checking whether {@code getAssetIdentifiers()} is empty and decide how to handle that for the affected
     * parameter (typically by leaving the parameter without an asset reference for the user to re-attach after
     * migration completes).
     * </p>
     *
     * @param sourceAssetId the identifier of the source asset
     * @return an asset reference for the newly copied asset, or an asset reference with no identifiers when the
     *         source asset could not be located
     * @throws IllegalArgumentException when {@code sourceAssetId} is null or blank
     * @throws IllegalStateException when invoked on an uploaded-payload migration context; assets are only
     *                               available when the migration source is a local Versioned Process Group
     */
    AssetReference copyAssetFromSource(String sourceAssetId);

    /**
     * Records configuration properties to merge into the named {@link ConfigurationStep}.
     * A {@code null} value removes the property.
     *
     * @param stepName configuration step name
     * @param propertyValues properties to record
     * @throws IllegalStateException when called outside {@code migrateConfiguration(...)}
     */
    void setProperties(String stepName, Map<String, String> propertyValues);

    /**
     * Records configuration properties that replace the named {@link ConfigurationStep}.
     * Properties not included in {@code propertyValues} are removed.
     *
     * @param stepName configuration step name
     * @param propertyValues properties to record
     * @throws IllegalStateException when called outside {@code migrateConfiguration(...)}
     */
    void replaceProperties(String stepName, Map<String, String> propertyValues);

    /**
     * Records a single property value reference to merge into the named {@link ConfigurationStep}. This allows a
     * property to be set directly to a {@link ConnectorValueReference}, such as an {@link AssetReference} returned by
     * {@link #copyAssetFromSource(String)}, rather than a plain string value. A {@code null} value reference removes
     * the property.
     *
     * @param stepName configuration step name
     * @param propertyName the name of the property to set
     * @param valueReference the value reference to record, or {@code null} to remove the property
     * @throws IllegalStateException when called outside {@code migrateConfiguration(...)}
     */
    void setValueReference(String stepName, String propertyName, ConnectorValueReference valueReference);

    /**
     * Records property value references to merge into the named {@link ConfigurationStep}. This allows properties to be
     * set directly to {@link ConnectorValueReference}s, such as {@link AssetReference}s returned by
     * {@link #copyAssetFromSource(String)}, rather than plain string values. A {@code null} value for a property
     * removes that property.
     *
     * @param stepName configuration step name
     * @param valueReferences the value references to record, keyed by property name
     * @throws IllegalStateException when called outside {@code migrateConfiguration(...)}
     */
    void setValueReferences(String stepName, Map<String, ConnectorValueReference> valueReferences);

    /**
     * Records the {@link VersionedComponentState} for a managed component.
     * Repeated calls for the same component replace prior recorded state; an empty state clears previously recorded state.
     *
     * @param managedComponentId managed Processor or Controller Service versioned identifier
     * @param state state to write
     * @throws IllegalArgumentException when {@code managedComponentId} is blank or {@code state} is {@code null}
     * @throws IllegalStateException when called outside {@code migrateState(...)}
     */
    void setComponentState(String managedComponentId, VersionedComponentState state);
}
