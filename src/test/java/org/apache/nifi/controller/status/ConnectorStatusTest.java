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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorStatusTest {

    @Test
    void connectorAttributesDefaultToEmptyAndAreNeverNull() {
        final ConnectorStatus status = new ConnectorStatus();
        assertNotNull(status.getConnectorAttributes());
        assertTrue(status.getConnectorAttributes().isEmpty());

        status.setConnectorAttributes(null);
        assertNotNull(status.getConnectorAttributes());
        assertTrue(status.getConnectorAttributes().isEmpty());
    }

    @Test
    void setConnectorAttributesStoresDefensiveCopy() {
        final ConnectorStatus status = new ConnectorStatus();
        final Map<String, String> source = new HashMap<>();
        source.put("connectorId", "id-1");
        status.setConnectorAttributes(source);

        // Mutating the source after the call must not affect the stored attributes.
        source.put("connectorName", "added-after");

        assertEquals(Map.of("connectorId", "id-1"), status.getConnectorAttributes());
    }

    @Test
    void getConnectorAttributesReturnsImmutableView() {
        final ConnectorStatus status = new ConnectorStatus();
        status.setConnectorAttributes(Map.of("connectorId", "id-1"));
        assertThrows(UnsupportedOperationException.class, () -> status.getConnectorAttributes().put("connectorName", "x"));
    }

    @Test
    void cloneCopiesConnectorAttributesIndependently() {
        final ConnectorStatus status = new ConnectorStatus();
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("connectorId", "id-1");
        attributes.put("connectorDefinitionId", "def-1");
        status.setConnectorAttributes(attributes);

        final ConnectorStatus clone = status.clone();
        assertEquals(status.getConnectorAttributes(), clone.getConnectorAttributes());

        // Mutating the original's attributes must not affect the clone.
        status.setConnectorAttributes(Map.of("connectorId", "id-2"));
        assertEquals("id-1", clone.getConnectorAttributes().get("connectorId"));
        assertEquals("def-1", clone.getConnectorAttributes().get("connectorDefinitionId"));
    }
}
