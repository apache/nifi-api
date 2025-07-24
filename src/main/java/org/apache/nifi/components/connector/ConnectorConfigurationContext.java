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

import java.util.List;

public interface ConnectorConfigurationContext extends Cloneable {

    ConnectorPropertyValue getProperty(String configurationStepName, String groupName, String propertyName);

    ConnectorPropertyValue getProperty(ConfigurationStep configurationStep, ConnectorPropertyGroup propertyGroup, ConnectorPropertyDescriptor propertyDescriptor);

    /**
     * Creates a new ConnectorConfigurationContext based on this context's values but with the provided property overrides applied.
     * @param stepName the name of the configuration step for which the overrides should be applied
     * @param groupConfigurations the list of PropertyGroupConfiguration objects containing the overrides
     * @return a new ConnectorConfigurationContext with the overrides applied
     */
    ConnectorConfigurationContext createWithOverrides(String stepName, List<PropertyGroupConfiguration> groupConfigurations);

    ConnectorConfigurationContext clone();
}
