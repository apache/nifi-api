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

/**
 * A view of a ConnectorConfigurationContext scoped to a specific configuration step.
 * Any changes to the underlying ConnectorConfigurationContext will be reflected in this context.
 */
public interface StepConfigurationContext {

    /**
     * Returns the value of the property with the given name.
     * @param propertyName the name of the property
     * @return the value of the property with the given name
     */
    ConnectorPropertyValue getProperty(String propertyName);

    /**
     * Returns the value of the property specified by the given descriptor.
     * @param propertyDescriptor the property descriptor
     * @return the value of the property specified by the given descriptor
     */
    ConnectorPropertyValue getProperty(ConnectorPropertyDescriptor propertyDescriptor);

    /**
     * Creates a new ConnectorConfigurationContext based on this context's values but with the provided property overrides applied.
     * @param propertyValues a map of property name to property value containing the overrides
     * @return a new ConnectorConfigurationContext with the overrides applied
     */
    StepConfigurationContext createWithOverrides(Map<String, String> propertyValues);

    /**
     * Returns a map of all property names to their corresponding values
     * @return a map of all property names to their corresponding values
     */
    Map<String, ConnectorPropertyValue> getProperties();
}
