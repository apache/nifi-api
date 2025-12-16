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
package org.apache.nifi.web;

import org.apache.nifi.components.connector.components.FlowContext;

/**
 * NiFi web context providing access to Connector instances for
 * connector custom UIs.
 */
public interface NiFiConnectorWebContext {

    /**
     * Returns the Connector Web Context for the given connector ID.
     * The returned Connector can be cast to a connector-specific interface
     * if the custom UI's classloader has visibility to that interface
     * (typically through the NAR classloader hierarchy). Active and
     * working flow context are provided for invoking connector methods
     * on components within each of those flow contexts.
     *
     * @param <T> the expected type of the Connector
     * @param connectorId the ID of the connector to retrieve
     * @return the ConnectorWebContext instance
     * @throws IllegalArgumentException if the connector does not exist
     */
    <T> ConnectorWebContext<T> getConnectorWebContext(String connectorId) throws IllegalArgumentException;

    /**
     * Hold the context needed to work with the Connector within a custom ui
     *
     * @param <T> the expected type of the Connector
     * @param connector the Connector instance
     * @param workingFlowContext the working {@link FlowContext} for the connector instance
     * @param activeFlowContext the active {@link FlowContext} for the connector instance
     */
    record ConnectorWebContext<T>(T connector, FlowContext workingFlowContext, FlowContext activeFlowContext) {
    }
}
