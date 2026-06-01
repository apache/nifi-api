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

package org.apache.nifi.components.connector;

import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.flow.VersionedExternalFlow;
import org.apache.nifi.logging.ComponentLog;

/**
 * <p>
 *     The ConnectorInitializationContext provides context about how the connector is being run.
 *     This includes the identifier and name of Connector as well as access to the crucial components that
 *     it may need to interact with in order to perform its tasks.
 * </p>
 */
public interface ConnectorInitializationContext {

    /**
     * Returns the identifier of the Connector.
     * @return the identifier of the Connector
     */
    String getIdentifier();

    /**
     * Returns the name of the Connector.
     * @return the name of the Connector
     */
    String getName();

    /**
     * Returns the ComponentLog that can be used for logging. Use of the ComponentLog is preferred
     * over directly constructing a Logger because it integrates with NiFi's logging system to create bulletins
     * as well as delegating to the underlying logging framework.
     *
     * @return the ComponentLog for logging
     */
    ComponentLog getLogger();

    /**
     * Returns the ComponentBundleLookup that can be used to determine which bundles are available
     * for a given component type.
     * @return the ComponentBundleLookup
     */
    ComponentBundleLookup getComponentBundleLookup();

    /**
     * <p>
     *   Updates the Connector's flow to the given VersionedExternalFlow. This may be a long-running process, as it involves
     *   several steps, to include:
     * </p>
     * <ul>
     *   <li>Identifying which elements in the flow have changed</li>
     *   <li>Stopping affected Processors and Controller Services, waiting for them to stop fully</li>
     *   <li>Applying necessary changes, to include changing component configuration, adding, and removing components</li>
     *   <li>Restarting all components</li>
     * </ul>
     *
     * <p>
     *     Depending on the changes required in order to update the flow to the provided VersionedProcessGroup, this
     *     could also result in stopping source processors and waiting for queues to drain, etc.
     * </p>
     *
     * <p>
     *   This method will block until the update is complete. Note that this could result in the associated flow becoming
     *   invalid if not properly configured. Otherwise, if the Connector is running, any newly added components will also
     *   be started.
     * </p>
     *
     * <p>
     *   This method uses the Bundle Compatability strategy of {@link BundleCompatibility#RESOLVE_BUNDLE}.
     * </p>
     *
     * @param flowContext the context of the flow to be updated
     * @param versionedExternalFlow the new representation of the flow
     */
    default void updateFlow(FlowContext flowContext, VersionedExternalFlow versionedExternalFlow) throws FlowUpdateException {
        updateFlow(flowContext, versionedExternalFlow, BundleCompatibility.RESOLVE_BUNDLE);
    }

    /**
     * <p>
     *   Updates the Connector's flow to the given VersionedExternalFlow with the specified bundle compatibility strategy.
     *   This method behaves like {@link #updateFlow(FlowContext, VersionedExternalFlow)} but allows control over how
     *   component bundles are resolved when the specified bundle is not available.
     * </p>
     *
     * <p>
     *    Note that if Bundle Compatability is not set to {@link BundleCompatibility#REQUIRE_EXACT_BUNDLE}, this method may update the provided
     *    VersionedExternalFlow to represent the actual bundles used during the update.
     * </p>
     *
     * @param flowContext the context of the flow to be updated
     * @param versionedExternalFlow the new representation of the flow
     * @param bundleCompatability the strategy to use when resolving component bundles
     * @throws FlowUpdateException if the flow update fails
     */
    void updateFlow(FlowContext flowContext, VersionedExternalFlow versionedExternalFlow, BundleCompatibility bundleCompatability) throws FlowUpdateException;

}
