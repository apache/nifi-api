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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a dependency that a ConfigurationStep has on another ConfigurationStep's property.
 * A ConfigurationStep can depend on a property of another step having some value (when dependentValues is null
 * and requiresAbsence is false), having one of a specific set of values (when dependentValues is non-null),
 * or requiring the property to be absent/null (when requiresAbsence is true).
 */
public final class ConfigurationStepDependency {
    private final String stepName;
    private final String propertyName;
    private final Set<String> dependentValues;

    /**
     * Creates a dependency on the specified step and property having one of the specified values.
     *
     * @param stepName the name of the step that this dependency is on
     * @param propertyName the name of the property that this dependency is on
     * @param dependentValues the set of values that the property must have; if the property has any
     *                        of these values, the dependency is satisfied
     */
    public ConfigurationStepDependency(final String stepName, final String propertyName, final Set<String> dependentValues) {
        this.stepName = Objects.requireNonNull(stepName, "Step name is required");
        this.propertyName = Objects.requireNonNull(propertyName, "Property name is required");
        this.dependentValues = dependentValues == null ? null : new HashSet<>(dependentValues);
    }

    /**
     * Creates a dependency on the specified step and property having some (any) value.
     *
     * @param stepName the name of the step that this dependency is on
     * @param propertyName the name of the property that this dependency is on
     */
    public ConfigurationStepDependency(final String stepName, final String propertyName) {
        this.stepName = Objects.requireNonNull(stepName, "Step name is required");
        this.propertyName = Objects.requireNonNull(propertyName, "Property name is required");
        this.dependentValues = null;
    }

    /**
     * @return the name of the ConfigurationStep that this dependency references
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * @return the name of the property within the referenced step that this dependency is on
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @return the set of values that the referenced property must have for the dependency to be satisfied,
     *         or null if the dependency is satisfied by the property having any value (or being absent if requiresAbsence is true)
     */
    public Set<String> getDependentValues() {
        return dependentValues;
    }


    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ConfigurationStepDependency that = (ConfigurationStepDependency) o;
        return Objects.equals(stepName, that.stepName)
            && Objects.equals(propertyName, that.propertyName)
            && Objects.equals(dependentValues, that.dependentValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepName, propertyName, dependentValues);
    }

    @Override
    public String toString() {
        return "ConfigurationStepDependency[stepName=" + stepName + ", propertyName=" + propertyName
            + ", dependentValues=" + dependentValues + "]";
    }
}

