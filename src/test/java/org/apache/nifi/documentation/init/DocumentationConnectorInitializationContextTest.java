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
package org.apache.nifi.documentation.init;

import org.apache.nifi.logging.ComponentLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentationConnectorInitializationContextTest {

    private DocumentationConnectorInitializationContext context;

    @BeforeEach
    void setUp() {
        context = new DocumentationConnectorInitializationContext();
    }

    @Test
    void testGetIdentifierReturnsNonNullUUID() {
        final String identifier = context.getIdentifier();

        assertNotNull(identifier);
    }

    @Test
    void testGetIdentifierReturnsConsistentValue() {
        final String identifier1 = context.getIdentifier();
        final String identifier2 = context.getIdentifier();

        assertEquals(identifier1, identifier2);
    }

    @Test
    void testDifferentInstancesHaveDifferentIdentifiers() {
        final DocumentationConnectorInitializationContext otherContext = new DocumentationConnectorInitializationContext();

        assertNotEquals(context.getIdentifier(), otherContext.getIdentifier());
    }

    @Test
    void testGetNameReturnsExpectedValue() {
        assertEquals("DocumentationConnector", context.getName());
    }

    @Test
    void testGetLoggerReturnsNopComponentLog() {
        final ComponentLog logger = context.getLogger();

        assertNotNull(logger);
        assertEquals(NopComponentLog.class, logger.getClass());
    }

    @Test
    void testUpdateFlowDoesNotThrow() throws Exception {
        // updateFlow is a no-op for documentation purposes, should not throw
        context.updateFlow(null, null);
    }
}

