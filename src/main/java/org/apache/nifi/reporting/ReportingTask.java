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
package org.apache.nifi.reporting;

import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.annotation.lifecycle.OnConfigurationRestored;
import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.migration.PropertyConfiguration;

/**
 * Defines a task that is responsible for reporting status information to
 * external destinations. All implementations of this class must have a default
 * constructor.
 *
 * <p>
 * <code>ReportingTask</code>s are discovered using Java's
 * <code>ServiceLoader</code> mechanism. As a result, all implementations must
 * follow these rules:
 *
 * <ul>
 * <li>The implementation must implement this interface.</li>
 * <li>The implementation must have a file named
 * org.apache.nifi.reporting.ReportingTask located within the jar's
 * <code>META-INF/services</code> directory. This file contains a list of
 * fully-qualified class names of all <code>ReportingTask</code>s in the jar,
 * one-per-line.
 * <li>The implementation must support a default constructor.</li>
 * </ul>
 *
 * <p>
 * ReportingTasks are scheduled on a delayed interval with a single thread.
 * Therefore, implementations are not required to be thread-safe.
 * </p>
 *
 * <p>
 * ReportingTasks may choose to annotate a method with the
 * {@link OnConfigurationRestored @OnConfigurationRestored} annotation. If this is done, that method
 * will be invoked after all properties have been set for the ReportingTask and
 * before it is scheduled to run. If the method throws an Exception, the
 * ReportingTask will be Administratively yielded and will not run for the
 * configured period of time.
 * </p>
 */
public interface ReportingTask extends ConfigurableComponent {

    /**
     * Provides the Reporting Task with access to objects that may be of use
     * throughout the life of the service
     *
     * @param config of initialization context
     * @throws org.apache.nifi.reporting.InitializationException if unable to init
     */
    void initialize(ReportingInitializationContext config) throws InitializationException;

    /**
     * This method is called on a scheduled interval to allow the Reporting Task
     * to perform its tasks.
     *
     * @param context reporting context
     */
    void onTrigger(ReportingContext context);

    /**
     * Indicates whether this reporting task, configured with the given {@link ReportingContext}, stores state.
     * @param context provides access to convenience methods for obtaining property values
     * @return True if this reporting task stores state
     */
    default boolean isStateful(ReportingContext context) {
        return this.getClass().isAnnotationPresent(Stateful.class);
    }

    /**
     * <p>
     * Allows for the migration of an old property configuration to a new configuration. This allows the Reporting Task to evolve over time,
     * as it allows properties to be renamed, removed, or reconfigured.
     * </p>
     *
     * <p>
     * This method is called only when a Reporting Task is restored from a previous configuration. For example, when NiFi is restarted and the
     * flow is restored from disk, when a previously configured flow is imported (e.g., from a JSON file that was exported or a NiFi Registry),
     * or when a node joins a cluster and inherits a flow that has a new Reporting Task. Once called, the method will not be invoked again for this
     * Reporting Task until NiFi is restarted.
     * </p>
     *
     * @param config the current property configuration
     */
    default void migrateProperties(PropertyConfiguration config) {
    }

}
