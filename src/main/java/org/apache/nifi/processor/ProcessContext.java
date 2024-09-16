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

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.context.ClusterContext;
import org.apache.nifi.context.PropertyContext;
import org.apache.nifi.controller.ControllerServiceLookup;
import org.apache.nifi.scheduling.ExecutionNode;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Provides a bridge between a Processor and the NiFi Framework
 * </p>
 *
 * <p>
 * <b>Note: </b>Implementations of this interface are NOT necessarily
 * thread-safe.
 * </p>
 */
public interface ProcessContext extends PropertyContext, ClusterContext {

    /**
     * Retrieves the current value set for the given descriptor, if a value is
     * set - else uses the descriptor to determine the appropriate default value
     *
     * @param propertyName of the property to lookup the value for
     * @return property value as retrieved by property name
     */
    PropertyValue getProperty(String propertyName);

    /**
     * Creates and returns a {@link PropertyValue} object that can be used for
     * evaluating the value of the given String
     *
     * @param rawValue the raw input before any property evaluation has occurred
     * @return a {@link PropertyValue} object that can be used for
     * evaluating the value of the given String
     */
    PropertyValue newPropertyValue(String rawValue);

    /**
     * <p>
     * Causes the Processor not to be scheduled for some pre-configured amount
     * of time. The duration of time for which the processor will not be
     * scheduled is configured in the same manner as the processor's scheduling
     * period.
     * </p>
     *
     * <p>
     * <b>Note: </b>This is NOT a blocking call and does not suspend execution
     * of the current thread.
     * </p>
     */
    void yield();

    /**
     * @return the maximum number of threads that may be executing this
     * processor's code at any given time
     */
    int getMaxConcurrentTasks();

    /**
     * @return the Nodes where this processor will be scheduled to run.
     */
    ExecutionNode getExecutionNode();

    /**
     * @return the annotation data configured for this processor
     */
    String getAnnotationData();

    /**
     * @return a Map of all PropertyDescriptors to their configured values. This
     * Map may or may not be modifiable, but modifying its values will not
     * change the values of the processor's properties
     */
    Map<PropertyDescriptor, String> getProperties();

    /**
     * @return a {@code ControllerServiceLookup} that can be used to obtain a
     * Controller Service
     */
    ControllerServiceLookup getControllerServiceLookup();

    /**
     * @return the set of all relationships for which space is available to
     * receive new objects
     */
    Set<Relationship> getAvailableRelationships();

    /**
     * Indicates whether or not the given relationship is configured to be auto-terminated
     * @param relationship the relationship
     * @return <code>true</code> if the given relationship is auto-terminated, <code>false</code> otherwise
     */
    boolean isAutoTerminated(Relationship relationship);

    /**
     * @return true if the processor has one or more incoming connections,
     * false otherwise
     */
    boolean hasIncomingConnection();

    /**
     * @return <code>true</code> if the processor has one or more incoming connections for
     *         which the source of the connection is NOT the processor; returns <code>false</code> if
     *         the processor has no incoming connections or if all incoming connections are self-loops
     *         (i.e., the processor is also the source of all incoming connections).
     */
    boolean hasNonLoopConnection();

    /**
     * @param relationship a relationship to check for connections
     * @return true if the relationship has one or more outbound connections,
     *         false otherwise
     */
    boolean hasConnection(Relationship relationship);

    /**
     * @param property the Property whose value should be inspected to determined if it contains an Expression Language Expression
     * @return <code>true</code> if the value of the given Property contains a NiFi Expression
     * Language Expression, <code>false</code> if it does not. Note that <code>false</code> will be returned if the Property Descriptor
     * does not allow the Expression Language, even if a seemingly valid Expression is present in the value.
     */
    boolean isExpressionLanguagePresent(PropertyDescriptor property);

    /**
     * @return the StateManager that can be used to store and retrieve state for this component
     */
    StateManager getStateManager();

    /**
     * @return the configured name of this processor
     */
    String getName();

    /**
     * @param relationship Relationship to be evaluated
     * @return the configured relationships to be retried of this processor
     */
    boolean isRelationshipRetried(Relationship relationship);

    /**
     * @return the max number of times that the Processor will retry a FlowFile that is routed to a Relationship that is marked for Retry
     */
    int getRetryCount();
}
