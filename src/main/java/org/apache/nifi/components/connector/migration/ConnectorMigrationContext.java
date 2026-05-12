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
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.flow.VersionedExternalFlow;

/**
 * Context provided to a Connector when evaluating or performing migration from a Versioned Process Group export. The
 * source flow exposed through this context is a read-only reference that the Connector uses to update its own managed
 * flow; the source flow itself is never installed onto the Connector.
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
     *
     * @return the active flow context
     */
    FlowContext getActiveFlowContext();

    /**
     * Copies the referenced source asset into the Connector asset namespace. When the source asset cannot be located
     * in the source asset manager (for example because it was deleted from the local source after the migration
     * request started), the framework logs a warning and returns an {@link AssetReference} with no asset identifiers
     * so the Connector can continue the migration without the missing asset. Callers should treat an empty asset
     * reference as "asset not migrated" and decide how to handle that for the affected parameter (typically by
     * leaving the parameter without an asset reference for the user to re-attach after migration completes).
     *
     * @param sourceAssetId the identifier of the source asset
     * @return an asset reference for the newly copied asset, or an asset reference with no identifiers when the
     *         source asset could not be located
     * @throws IllegalArgumentException when {@code sourceAssetId} is null or blank, or when invoked for an uploaded
     *                                  payload migration (assets are not available on the uploaded-payload path)
     */
    AssetReference copyAssetFromSource(String sourceAssetId);
}
