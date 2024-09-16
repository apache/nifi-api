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
package org.apache.nifi.controller;

import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.flowanalysis.FlowAnalysisRule;
import org.apache.nifi.migration.PropertyConfiguration;
import org.apache.nifi.parameter.ParameterProvider;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSessionFactory;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.reporting.ReportingTask;

/**
 * <p>
 * This interface provides a mechanism for creating services that are shared
 * among all {@link Processor}s, {@link ReportingTask}s, {@link FlowAnalysisRule}s, {@link ParameterProvider}s and other
 * {@code ControllerService}s.
 * </p>
 *
 * <p>
 * <code>ControllerService</code>s are discovered using Java's
 * <code>ServiceLoader</code> mechanism. As a result, all implementations must
 * follow these rules:
 * </p>
 *
 * <ul>
 * <li>The implementation must implement this interface.</li>
 * <li>The implementation must have a file named
 * org.apache.nifi.controller.ControllerService located within the jar's
 * <code>META-INF/services</code> directory. This file contains a list of
 * fully-qualified class names of all <code>ControllerService</code>s in the
 * jar, one-per-line.
 * <li>The implementation must support a default constructor.</li>
 * </ul>
 *
 * <p>
 * <b>All implementations of this interface must be thread-safe.</b>
 * </p>
 *
 * <h2>Accessing Controller Services</h2>
 * <p>
 * A ControllerService is accessible only through its interface. The framework
 * provides access to a ControllerService through two different mechanisms:
 * <ul>
 * <li>
 * A {@link PropertyDescriptor} can be created via the
 * {@link PropertyDescriptor.Builder} after calling the
 * {@link PropertyDescriptor.Builder#identifiesControllerService(Class) identifiesControllerService(Class)}
 * method and then the ControllerService is accessed via
 * {@link PropertyValue#asControllerService(Class)} method.
 * <p>
 * For example:
 * </p>
 * <pre><code>
 *    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor.Builder()
 *     .name("My Property")
 *     .description("Example Property")
 *     .identifiesControllerService( MyControllerServiceInterface.class )
 *     .build();
 *
 *    ...
 *    public void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException {
 *     // Obtain the user-selected controller service
 *     final MyControllerServiceInterface service = context.getProperty(MY_PROPERTY).asControllerService( MyControllerServiceInterface.class );
 *     ...
 *    }
 *
 * </code></pre>
 * <li>A Controller Service can be obtained via a
 * {@link ControllerServiceLookup}. This lookup may be obtained, for example,
 * from the {@link ProcessContext} that is provided to a {@link Processor}'s
 * {@link Processor#onTrigger(ProcessContext, ProcessSessionFactory) onTrigger}
 * method.
 * <p>
 * For example:
 * </p>
 *
 * <pre><code>
 *    public void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException {
 *      final MyControllerServiceInterface service = (MyControllerServiceInterface) context.getControllerServiceLookup().getControllerService("service_identifier");
 *    }
 * </code></pre>
 * </li>
 * </ul>
 *
 * <h2>Defining a Controller Service</h2>
 * <p>
 * Note in both of the examples above, that the Controller Service was accessed
 * only by its interface, and this interface extends ControllerService. If we
 * have an implementation named MyServiceImpl, for example, that implements
 * MyControllerServiceInterface, we cannot, in either case, attempt to cast the
 * ControllerService to the desired implementation. Doing so will result in a
 * {@link ClassCastException}. This is by design and is done for the following
 * reasons:
 *
 * <ul>
 * <li>
 * It is a good coding practice to implement such a service as an interface in
 * general.
 * </li>
 * <li>
 * A Controller Service can be referenced from different NiFi Archives (NARs).
 * This means that the Controller Service may be defined in one ClassLoader and
 * referenced from another unrelated ClassLoader. In order to account for this,
 * NiFi will change the current thread's ClassLoader as appropriate when
 * entering the Controller Service's code and revert back to the previous
 * ClassLoader after exiting the Controller Service's code.
 * </li>
 * </ul>
 *
 * <h2>Controller Services and NARs</h2>
 * <p>
 * Due to the fact that a Controller Service may be referenced from a different
 * NAR than the one in which the implementation lives, it is crucial that both
 * the Controller Service's interface and the code referencing the interface
 * inherit from the same ClassLoader. This is accomplished by ensuring that the
 * NAR that contains the Controller Service interface is the parent (or
 * ancestor) of the NAR that references the Controller Service interface.
 * </p>
 *
 * <p>
 * Typically, this is done by creating a NAR structure as follows:
 * <pre>
 *   + my-services-api-nar
 *   +--- service-X-implementation-nar
 *   +--- service-Y-implementation-nar
 *   +--- service-Z-implementation-nar
 *   +--- processor-A-nar
 *   +--- processor-B-nar
 * </pre>
 *
 * <p>
 * In this case, the {@code MyControllerServiceInterface} interface, and any
 * other Controller Service interfaces, will be defined in the
 * {@code my-services-api-nar} NAR. Implementations are then encapsulated within
 * the {@code service-X-implementation-nar},
 * {@code service-Y-implementation-nar}, and
 * {@code service-Z-implementation-nar} NARs. All Controller Services and all
 * Processors defined in these NARs are able to reference any other Controller
 * Services whose interfaces are provided in the {@code my-services-api-nar}
 * NAR.
 * </p>
 *
 * <p>
 * For more information on NARs, see the NiFi Developer Guide.
 * </p>
 */
public interface ControllerService extends ConfigurableComponent {

    /**
     * Provides the Controller Service with access to objects that may be of use
     * throughout the life of the service. This method will be called before any
     * properties are set
     *
     * @param context of initialization
     * @throws org.apache.nifi.reporting.InitializationException if unable to init
     */
    void initialize(ControllerServiceInitializationContext context) throws InitializationException;

    /**
     * Indicates whether this controller service, configured with the given {@link ConfigurationContext}, stores state.
     * @param context provides access to convenience methods for obtaining property values
     * @return True if this controller service stores state
     */
    default boolean isStateful(ConfigurationContext context) {
        return this.getClass().isAnnotationPresent(Stateful.class);
    }

    /**
     * <p>
     * Allows for the migration of an old property configuration to a new configuration. This allows the Controller Service to evolve over time,
     * as it allows properties to be renamed, removed, or reconfigured.
     * </p>
     *
     * <p>
     * This method is called only when a Controller Service is restored from a previous configuration. For example, when NiFi is restarted and the
     * flow is restored from disk, when a previously configured flow is imported (e.g., from a JSON file that was exported or a NiFi Registry),
     * or when a node joins a cluster and inherits a flow that has a new Controller Service. Once called, the method will not be invoked again for this
     * Controller Service until NiFi is restarted.
     * </p>
     *
     * @param config the current property configuration
     */
    default void migrateProperties(PropertyConfiguration config) {
    }

}
