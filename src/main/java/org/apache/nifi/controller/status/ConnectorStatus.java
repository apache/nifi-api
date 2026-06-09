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
package org.apache.nifi.controller.status;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The status of a Connector, including the status of its managed root Process Group.
 *
 * <p>
 *     Connector-managed flows live outside the controller's root Process Group, so reporting tasks that wish to observe
 *     metrics for them must consult {@link org.apache.nifi.reporting.EventAccess#getConnectorStatuses()} in addition to
 *     {@link org.apache.nifi.reporting.EventAccess#getControllerStatus()}. Each {@link ConnectorStatus} exposes the
 *     Connector's identity along with the {@link ProcessGroupStatus} for its managed root group.
 * </p>
 *
 * <p>
 *     The Connector API is experimental and this type is expected to grow over time to surface connector-level metrics
 *     that do not exist at the Process Group level (e.g. connector-wide backlog or idle time).
 * </p>
 */
public class ConnectorStatus implements Cloneable {

    private String id;
    private String name;
    private ProcessGroupStatus rootGroupStatus;
    private Map<String, String> connectorAttributes = Map.of();

    /**
     * @return the identifier of the Connector
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the name of the Connector
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the status of the Connector's managed root Process Group, or {@code null} if the Connector has no
     *         active managed flow at the time the status was captured
     */
    public ProcessGroupStatus getRootGroupStatus() {
        return rootGroupStatus;
    }

    public void setRootGroupStatus(final ProcessGroupStatus rootGroupStatus) {
        this.rootGroupStatus = rootGroupStatus;
    }

    /**
     * @return an immutable map of Connector-level attributes (for example, the Connector identifier, name, component
     *         type, and bundle coordinate, along with any provider-supplied attributes). These describe the Connector
     *         as a whole and are intended to be applied to logs and metrics emitted for its managed flow. Never
     *         {@code null}.
     */
    public Map<String, String> getConnectorAttributes() {
        return Collections.unmodifiableMap(connectorAttributes);
    }

    public void setConnectorAttributes(final Map<String, String> connectorAttributes) {
        this.connectorAttributes = connectorAttributes == null ? Map.of() : new LinkedHashMap<>(connectorAttributes);
    }

    @Override
    public ConnectorStatus clone() {
        final ConnectorStatus clonedObj = new ConnectorStatus();
        clonedObj.id = id;
        clonedObj.name = name;
        clonedObj.rootGroupStatus = rootGroupStatus == null ? null : rootGroupStatus.clone();
        clonedObj.connectorAttributes = new LinkedHashMap<>(connectorAttributes);
        return clonedObj;
    }

    @Override
    public String toString() {
        return "ConnectorStatus [id=" + id
                + ", name=" + name
                + ", rootGroupStatus=" + rootGroupStatus
                + ", connectorAttributes=" + connectorAttributes
                + "]";
    }
}
