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

/**
 * NiFi web context providing access to Connector instances for
 * connector custom UIs.
 */
public interface NiFiConnectorWebContext {

    /**
     * Returns the Connector instance for the given connector ID.
     * The returned Connector can be cast to a connector-specific interface
     * if the custom UI's classloader has visibility to that interface
     * (typically through the NAR classloader hierarchy).
     *
     * @param <T> the expected type of the Connector
     * @param connectorId the ID of the connector to retrieve
     * @return the Connector instance
     * @throws IllegalArgumentException if the connector does not exist
     */
    <T> T getConnector(String connectorId) throws IllegalArgumentException;
}
