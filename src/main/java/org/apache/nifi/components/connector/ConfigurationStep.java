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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConfigurationStep {
    private final String name;
    private final String description;
    private final List<ConnectorPropertyGroup> propertyGroups;
    private final Set<ConfigurationStepDependency> dependencies;

    private ConfigurationStep(final Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.propertyGroups = Collections.unmodifiableList(builder.propertyGroups);
        this.dependencies = Collections.unmodifiableSet(builder.dependencies);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ConnectorPropertyGroup> getPropertyGroups() {
        return propertyGroups;
    }

    /**
     * @return the set of dependencies that this step has on other steps' properties
     */
    public Set<ConfigurationStepDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "ConfigurationStep[name=" + name + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ConfigurationStep that = (ConfigurationStep) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public static final class Builder {
        private String name;
        private String description;
        private List<ConnectorPropertyGroup> propertyGroups = Collections.emptyList();
        private final Set<ConfigurationStepDependency> dependencies = new HashSet<>();

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder propertyGroups(final List<ConnectorPropertyGroup> propertyGroups) {
            this.propertyGroups = new ArrayList<>(propertyGroups);
            return this;
        }

        /**
         * Sets a dependency on another ConfigurationStep's property having some (any) value configured.
         *
         * @param step the ConfigurationStep that this step depends on
         * @param property the property within the specified step that must have a value
         * @return this Builder for method chaining
         */
        public Builder dependsOn(final ConfigurationStep step, final ConnectorPropertyDescriptor property) {
            dependencies.add(new ConfigurationStepDependency(step.getName(), property.getName()));
            return this;
        }

        /**
         * Sets a dependency on another ConfigurationStep's property having one of the specified values.
         *
         * @param step the ConfigurationStep that this step depends on
         * @param property the property within the specified step that must have one of the specified values
         * @param dependentValues the list of values that satisfy this dependency
         * @return this Builder for method chaining
         */
        public Builder dependsOn(final ConfigurationStep step, final ConnectorPropertyDescriptor property, final List<DescribedValue> dependentValues) {
            if (dependentValues == null || dependentValues.isEmpty()) {
                dependencies.add(new ConfigurationStepDependency(step.getName(), property.getName()));
            } else {
                final Set<String> dependentValueSet = dependentValues.stream()
                    .map(DescribedValue::getValue)
                    .collect(Collectors.toSet());

                dependencies.add(new ConfigurationStepDependency(step.getName(), property.getName(), dependentValueSet));
            }

            return this;
        }

        /**
         * Sets a dependency on another ConfigurationStep's property having one of the specified values.
         *
         * @param step the ConfigurationStep that this step depends on
         * @param property the property within the specified step that must have one of the specified values
         * @param firstDependentValue the first value that satisfies this dependency
         * @param additionalDependentValues additional values that satisfy this dependency
         * @return this Builder for method chaining
         */
        public Builder dependsOn(final ConfigurationStep step, final ConnectorPropertyDescriptor property,
                final DescribedValue firstDependentValue, final DescribedValue... additionalDependentValues) {

            final List<DescribedValue> dependentValues = new ArrayList<>();
            dependentValues.add(firstDependentValue);
            dependentValues.addAll(Arrays.asList(additionalDependentValues));
            return dependsOn(step, property, dependentValues);
        }

        /**
         * Sets a dependency on another ConfigurationStep's property having one of the specified string values.
         *
         * @param step the ConfigurationStep that this step depends on
         * @param property the property within the specified step that must have one of the specified values
         * @param dependentValues the string values that satisfy this dependency
         * @return this Builder for method chaining
         */
        public Builder dependsOn(final ConfigurationStep step, final ConnectorPropertyDescriptor property, final String... dependentValues) {
            final List<DescribedValue> describedValues = Arrays.stream(dependentValues)
                .map(AllowableValue::new)
                .map(DescribedValue.class::cast)
                .toList();

            return dependsOn(step, property, describedValues);
        }

        public ConfigurationStep build() {
            if (name == null) {
                throw new IllegalStateException("Configuration Step's name must be provided");
            }

            // Ensure that all Property Descriptor names are unique
            final Set<String> propertyNames = new HashSet<>();
            for (final ConnectorPropertyGroup propertyGroup : propertyGroups) {
                for (final ConnectorPropertyDescriptor descriptor : propertyGroup.getProperties()) {
                    if (!propertyNames.add(descriptor.getName())) {
                        throw new IllegalStateException("All Property Descriptor names must be unique within a Configuration Step. Duplicate name found: " + descriptor.getName());
                    }
                }
            }

            return new ConfigurationStep(this);
        }
    }
}
