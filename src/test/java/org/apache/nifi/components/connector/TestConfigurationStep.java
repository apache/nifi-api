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

import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.DescribedValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConfigurationStep {

    @Test
    void testDependsOnWithStepAndProperty() {
        final ConnectorPropertyDescriptor connectionTypeProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Connection Type")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep connectionStep = new ConfigurationStep.Builder()
            .name("Connection")
            .description("Connection configuration")
            .build();

        final ConfigurationStep authenticationStep = new ConfigurationStep.Builder()
            .name("Authentication")
            .description("Authentication configuration")
            .dependsOn(connectionStep, connectionTypeProperty)
            .build();

        final Set<ConfigurationStepDependency> dependencies = authenticationStep.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertEquals("Connection", dependency.getStepName());
        assertEquals("Connection Type", dependency.getPropertyName());
        assertNull(dependency.getDependentValues());
    }

    @Test
    void testDependsOnWithSpecificValues() {
        final ConnectorPropertyDescriptor connectionTypeProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Connection Type")
            .type(PropertyType.STRING)
            .allowableValues("HTTP", "HTTPS", "FTP")
            .build();

        final ConfigurationStep connectionStep = new ConfigurationStep.Builder()
            .name("Connection")
            .description("Connection configuration")
            .build();

        final ConfigurationStep tlsStep = new ConfigurationStep.Builder()
            .name("TLS Configuration")
            .description("TLS settings")
            .dependsOn(connectionStep, connectionTypeProperty, new AllowableValue("HTTPS"))
            .build();

        final Set<ConfigurationStepDependency> dependencies = tlsStep.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertEquals("Connection", dependency.getStepName());
        assertEquals("Connection Type", dependency.getPropertyName());
        assertNotNull(dependency.getDependentValues());
        assertEquals(Set.of("HTTPS"), dependency.getDependentValues());
    }

    @Test
    void testDependsOnWithMultipleDescribedValues() {
        final ConnectorPropertyDescriptor authTypeProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Auth Type")
            .type(PropertyType.STRING)
            .allowableValues("NONE", "BASIC", "OAUTH", "API_KEY")
            .build();

        final ConfigurationStep authStep = new ConfigurationStep.Builder()
            .name("Authentication")
            .build();

        final ConfigurationStep credentialsStep = new ConfigurationStep.Builder()
            .name("Credentials")
            .description("Credential configuration")
            .dependsOn(authStep, authTypeProperty, new AllowableValue("BASIC"), new AllowableValue("OAUTH"), new AllowableValue("API_KEY"))
            .build();

        final Set<ConfigurationStepDependency> dependencies = credentialsStep.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertEquals("Authentication", dependency.getStepName());
        assertEquals("Auth Type", dependency.getPropertyName());
        assertNotNull(dependency.getDependentValues());
        assertEquals(Set.of("BASIC", "OAUTH", "API_KEY"), dependency.getDependentValues());
    }

    @Test
    void testDependsOnWithStringValues() {
        final ConnectorPropertyDescriptor protocolProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Protocol")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep connectionStep = new ConfigurationStep.Builder()
            .name("Connection")
            .build();

        final ConfigurationStep securityStep = new ConfigurationStep.Builder()
            .name("Security")
            .dependsOn(connectionStep, protocolProperty, "HTTPS", "SFTP")
            .build();

        final Set<ConfigurationStepDependency> dependencies = securityStep.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertEquals("Connection", dependency.getStepName());
        assertEquals("Protocol", dependency.getPropertyName());
        assertNotNull(dependency.getDependentValues());
        assertEquals(Set.of("HTTPS", "SFTP"), dependency.getDependentValues());
    }

    @Test
    void testDependsOnWithDescribedValueList() {
        final ConnectorPropertyDescriptor modeProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Mode")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep setupStep = new ConfigurationStep.Builder()
            .name("Setup")
            .build();

        final ConfigurationStep advancedStep = new ConfigurationStep.Builder()
            .name("Advanced")
            .dependsOn(setupStep, modeProperty, List.of(new AllowableValue("advanced"), new AllowableValue("expert")))
            .build();

        final Set<ConfigurationStepDependency> dependencies = advancedStep.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertEquals("Setup", dependency.getStepName());
        assertEquals("Mode", dependency.getPropertyName());
        assertEquals(Set.of("advanced", "expert"), dependency.getDependentValues());
    }

    @Test
    void testMultipleDependencies() {
        final ConnectorPropertyDescriptor enabledProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Enabled")
            .type(PropertyType.BOOLEAN)
            .build();

        final ConnectorPropertyDescriptor modeProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Mode")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep step1 = new ConfigurationStep.Builder()
            .name("Step 1")
            .build();

        final ConfigurationStep step2 = new ConfigurationStep.Builder()
            .name("Step 2")
            .build();

        final ConfigurationStep step3 = new ConfigurationStep.Builder()
            .name("Step 3")
            .dependsOn(step1, enabledProperty, "true")
            .dependsOn(step2, modeProperty, "advanced")
            .build();

        final Set<ConfigurationStepDependency> dependencies = step3.getDependencies();
        assertEquals(2, dependencies.size());

        boolean foundStep1Dependency = false;
        boolean foundStep2Dependency = false;
        for (final ConfigurationStepDependency dependency : dependencies) {
            if ("Step 1".equals(dependency.getStepName())) {
                assertEquals("Enabled", dependency.getPropertyName());
                assertEquals(Set.of("true"), dependency.getDependentValues());
                foundStep1Dependency = true;
            } else if ("Step 2".equals(dependency.getStepName())) {
                assertEquals("Mode", dependency.getPropertyName());
                assertEquals(Set.of("advanced"), dependency.getDependentValues());
                foundStep2Dependency = true;
            }
        }

        assertTrue(foundStep1Dependency, "Expected dependency on Step 1 not found");
        assertTrue(foundStep2Dependency, "Expected dependency on Step 2 not found");
    }

    @Test
    void testNoDependencies() {
        final ConfigurationStep step = new ConfigurationStep.Builder()
            .name("Standalone Step")
            .description("A step with no dependencies")
            .build();

        assertNotNull(step.getDependencies());
        assertTrue(step.getDependencies().isEmpty());
    }

    @Test
    void testDependsOnWithEmptyValueListTreatedAsAnyValue() {
        final ConnectorPropertyDescriptor property = new ConnectorPropertyDescriptor.Builder()
            .name("Some Property")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep step1 = new ConfigurationStep.Builder()
            .name("Step 1")
            .build();

        final ConfigurationStep step2 = new ConfigurationStep.Builder()
            .name("Step 2")
            .dependsOn(step1, property, List.of())
            .build();

        final Set<ConfigurationStepDependency> dependencies = step2.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertNull(dependency.getDependentValues());
    }

    @Test
    void testDependsOnAbsence() {
        final ConnectorPropertyDescriptor optionalProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Optional Feature")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep featureStep = new ConfigurationStep.Builder()
            .name("Feature Configuration")
            .build();

        final ConfigurationStep alternativeStep = new ConfigurationStep.Builder()
            .name("Alternative Configuration")
            .description("Only shown when optional feature is not configured")
            .dependsOn(featureStep, optionalProperty, DescribedValue.NULL)
            .build();

        final Set<ConfigurationStepDependency> dependencies = alternativeStep.getDependencies();
        assertEquals(1, dependencies.size());

        final ConfigurationStepDependency dependency = dependencies.iterator().next();
        assertEquals("Feature Configuration", dependency.getStepName());
        assertEquals("Optional Feature", dependency.getPropertyName());
        assertNotNull(dependency.getDependentValues());
        assertTrue(dependency.getDependentValues().contains(null));
    }

    @Test
    void testDependsOnAbsenceWithRegularDependency() {
        final ConnectorPropertyDescriptor modeProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Mode")
            .type(PropertyType.STRING)
            .build();

        final ConnectorPropertyDescriptor customProperty = new ConnectorPropertyDescriptor.Builder()
            .name("Custom Setting")
            .type(PropertyType.STRING)
            .build();

        final ConfigurationStep modeStep = new ConfigurationStep.Builder()
            .name("Mode Selection")
            .build();

        final ConfigurationStep customStep = new ConfigurationStep.Builder()
            .name("Custom Configuration")
            .build();

        final ConfigurationStep defaultStep = new ConfigurationStep.Builder()
            .name("Default Configuration")
            .dependsOn(modeStep, modeProperty, "default")
            .dependsOn(customStep, customProperty, DescribedValue.NULL)
            .build();

        final Set<ConfigurationStepDependency> dependencies = defaultStep.getDependencies();
        assertEquals(2, dependencies.size());

        boolean foundModeDependency = false;
        boolean foundAbsenceDependency = false;
        for (final ConfigurationStepDependency dependency : dependencies) {
            if ("Mode Selection".equals(dependency.getStepName())) {
                assertEquals("Mode", dependency.getPropertyName());
                assertEquals(Set.of("default"), dependency.getDependentValues());
                foundModeDependency = true;
            } else if ("Custom Configuration".equals(dependency.getStepName())) {
                assertEquals("Custom Setting", dependency.getPropertyName());
                assertNotNull(dependency.getDependentValues());
                assertTrue(dependency.getDependentValues().contains(null));
                foundAbsenceDependency = true;
            }
        }

        assertTrue(foundModeDependency, "Expected mode dependency not found");
        assertTrue(foundAbsenceDependency, "Expected absence dependency not found");
    }
}

