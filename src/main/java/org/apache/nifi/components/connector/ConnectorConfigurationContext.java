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

import java.util.Map;
import java.util.Set;

public interface ConnectorConfigurationContext extends Cloneable {

    /**
     * Returns the property value for the given property name in the specified configuration step.
     * @param configurationStepName the name of the configuration step
     * @param propertyName the name of the property
     * @return the property value for the given property name in the specified configuration step
     */
    ConnectorPropertyValue getProperty(String configurationStepName, String propertyName);

    /**
     * Returns the property value for the given property descriptor in the specified configuration step.
     * @param configurationStep the configuration step
     * @param propertyDescriptor the property descriptor
     * @return the property value for the given property descriptor in the specified configuration step
     */
    ConnectorPropertyValue getProperty(ConfigurationStep configurationStep, ConnectorPropertyDescriptor propertyDescriptor);

    /**
     * Returns a set of all property names for the specified configuration step.
     * @param configurationStepName the name of the configuration step
     * @return a set of all property names for the specified configuration step
     */
    Set<String> getPropertyNames(String configurationStepName);

    /**
     * Returns a set of all property names for the specified configuration step.
     * @param configurationStep the configuration step
     * @return a set of all property names for the specified configuration step
     */
    Set<String> getPropertyNames(ConfigurationStep configurationStep);

    /**
     * Returns a view of this configuration context scoped to the provided step name.
     * @param stepName the name of the configuration step
     * @return a StepConfigurationContext scoped to the provided step name
     */
    StepConfigurationContext scopedToStep(String stepName);

    /**
     * Returns a view of this configuration context scoped to the provided configuration step.
     * @param configurationStep the configuration step
     * @return a StepConfigurationContext scoped to the provided configuration step
     */
    StepConfigurationContext scopedToStep(ConfigurationStep configurationStep);

    /**
     * Creates a new ConnectorConfigurationContext based on this context's values but with the
     * values for the given step overridden. If the provided map of values does not override all properties
     * for the step, the remaining properties will retain their existing values. Said another way, this is a
     * "partial override" for the specified step. If the provided map contains a null value for a property,
     * the returned context will have that property removed.
     *
     * @param stepName the name of the configuration step for which the overrides should be applied
     * @param propertyValues a map of property name to property value containing the overrides
     * @return a new ConnectorConfigurationContext with the overrides applied
     */
    ConnectorConfigurationContext createWithOverrides(String stepName, Map<String, String> propertyValues);

    /**
     * Creates a deep copy of this ConnectorConfigurationContext.
     * @return a deep copy of this ConnectorConfigurationContext
     */
    ConnectorConfigurationContext clone();
}
