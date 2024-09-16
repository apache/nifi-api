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
package org.apache.nifi.processor;

import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.migration.PropertyConfiguration;
import org.apache.nifi.migration.RelationshipConfiguration;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.Set;

/**
 * <p>
 * Processor objects operate on FlowFile objects where the processors are linked
 * together via relationships forming a directed graph structure.</p>
 *
 * <p>
 * The validity of a processor once established is considered safe until a
 * configuration property of that processor is changed. Even if the property
 * change is itself valid it still brings the validity of the processor as a
 * whole into question. Therefore changing a processor as a whole must be
 * reverified. Also, changing any configuration property of a processor can also
 * imply that its supported relationships have changed.</p>
 *
 * <p>
 * Each Processor object is a single node in a flow graph. The same processor
 * object on a graph may be called concurrently to update its configuration
 * state and to perform 'onTrigger' actions. The framework also provides
 * numerous hooks via annotations that subclasses can use to control the
 * lifecycle of a given processor instance.</p>
 *
 * <p>
 * Processor objects are expected to be thread-safe and must have a public
 * default no-args constructor to facilitate the java service loader
 * mechanism.</p>
 *
 */
public interface Processor extends ConfigurableComponent {

    /**
     * Provides the processor with access to objects that may be of use
     * throughout the life of the Processor
     *
     * @param context of initialization
     */
    void initialize(ProcessorInitializationContext context);

    /**
     * @return Set of all relationships this processor expects to transfer a
     * flow file to. An empty set indicates this processor does not have any
     * destination relationships. Guaranteed non null.
     */
    Set<Relationship> getRelationships();

    /**
     * <p>
     * The method called when this processor is triggered to operate by the
     * controller. In the absence of the {@link org.apache.nifi.annotation.behavior.TriggerSerially} annotation,
     * this method may be called concurrently from different threads.
     * When this method is called depends on how this processor is
     * configured within a controller to be triggered (timing or event
     * based).</p>
     *
     * @param context provides access to convenience methods for obtaining
     * property values, delaying the scheduling of the processor, provides
     * access to Controller Services, etc.
     * @param sessionFactory provides access to a {@link ProcessSession}, which
     * can be used for accessing FlowFiles, etc.
     *
     * @throws ProcessException if processing did not complete normally though
     * indicates the problem is an understood potential outcome of processing.
     * The controller/caller will handle these exceptions gracefully such as
     * logging, etc.. If another type of exception is allowed to propagate the
     * controller may no longer trigger this processor to operate, as this would
     * indicate a probable coding defect.
     */
    void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException;

    /**
     * Indicates whether this processor, configured with the given {@link ProcessContext}, stores state.
     * @param context provides access to convenience methods for obtaining
     * property values, delaying the scheduling of the processor, provides
     * access to Controller Services, etc.
     * @return True if this processor stores state
     */
    default boolean isStateful(ProcessContext context) {
        return this.getClass().isAnnotationPresent(Stateful.class);
    }

    /**
     * <p>
     *     Allows for the migration of an old property configuration to a new configuration. This allows the Processor to evolve over time,
     *     as it allows properties to be renamed, removed, or reconfigured.
     * </p>
     *
     * <p>
     *     This method is called only when a Processor is restored from a previous configuration. For example, when NiFi is restarted and the
     *     flow is restored from disk, when a previously configured flow is imported (e.g., from a JSON file that was exported or a NiFi Registry),
     *     or when a node joins a cluster and inherits a flow that has a new Processor. Once called, the method will not be invoked again for this
     *     Processor until NiFi is restarted.
     * </p>
     *
     * @param config the current property configuration
     */
    default void migrateProperties(PropertyConfiguration config) {
    }

    /**
     * Allows for the migration of an old relationship configuration to a new configuration
     *
     * @param config the current relationship configuration
     */
    default void migrateRelationships(RelationshipConfiguration config) {
    }
}
