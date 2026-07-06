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

package org.apache.nifi.migration;

import org.apache.nifi.components.connector.ConfigurationStep;

import java.util.Set;

/**
 * Entry point for a {@link org.apache.nifi.components.connector.Connector} to migrate its persisted property
 * configuration on restore. Because a Connector's properties are organized into named {@link ConfigurationStep}s,
 * migration is scoped per step: this interface exposes step-level operations directly and yields a
 * {@link ConnectorStepPropertyConfiguration} for per-step property changes via {@link #forStep(String)}.
 *
 * <p>
 *     <b>Implementation Note:</b> This API is experimental and subject to change between minor releases.
 * </p>
 */
public interface ConnectorPropertyConfiguration {

    /**
     * @return the names of all configuration steps currently known to this configuration
     */
    Set<String> getStepNames();

    /**
     * @param stepName the name of the configuration step
     * @return <code>true</code> if this configuration contains the given step
     */
    boolean hasStep(String stepName);

    /**
     * Renames an existing configuration step, preserving all of its properties and their underlying value references.
     *
     * @param oldStepName the current step name
     * @param newStepName the new step name
     * @return <code>true</code> if the step was renamed; <code>false</code> if no step exists with {@code oldStepName}
     * @throws IllegalStateException if a step already exists with {@code newStepName}
     */
    boolean renameStep(String oldStepName, String newStepName);

    /**
     * Removes the configuration step with the given name and all of its properties, if it exists.
     *
     * @param stepName the name of the step to remove
     * @return <code>true</code> if the step was removed; <code>false</code> if the step did not exist
     */
    boolean removeStep(String stepName);

    /**
     * Returns a step-scoped view of this configuration. The view is usable even when no step currently exists with the
     * given name; the step is registered on the first property write. Retrieval alone does not register a step.
     *
     * @param stepName the name of the configuration step
     * @return a step-scoped configuration view
     */
    ConnectorStepPropertyConfiguration forStep(String stepName);

    /**
     * Convenience overload of {@link #forStep(String)} accepting a {@link ConfigurationStep}.
     */
    default ConnectorStepPropertyConfiguration forStep(final ConfigurationStep step) {
        return forStep(step.getName());
    }
}
