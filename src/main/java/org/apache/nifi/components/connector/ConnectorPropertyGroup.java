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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ConnectorPropertyGroup {
    private final String name;
    private final String description;
    private final List<ConnectorPropertyDescriptor> properties;

    private ConnectorPropertyGroup(final Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.properties = List.copyOf(builder.properties);
    }

    /**
     * Returns the name of the property sub-group.
     *
     * @return the name of the sub-group
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the property sub-group.
     *
     * @return the description of the sub-group
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the properties defined in this sub-group.
     *
     * @return the properties in this sub-group
     */
    public List<ConnectorPropertyDescriptor> getProperties() {
        return properties;
    }

    /**
     * Creates a new Builder for constructing ConnectorPropertySubGroup instances.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new Builder initialized with values from an existing ConnectorPropertySubGroup.
     *
     * @param subGroup the sub-group to copy values from
     * @return a new Builder with copied values
     */
    public static Builder builder(final ConnectorPropertyGroup subGroup) {
        return new Builder()
                .name(subGroup.getName())
                .description(subGroup.getDescription())
                .properties(subGroup.getProperties());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConnectorPropertyGroup that = (ConnectorPropertyGroup) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description) &&
               Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, properties);
    }

    @Override
    public String toString() {
        return "ConnectorPropertyGroup[" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", properties=" + properties +
               "]";
    }

    public static class Builder {
        private String name;
        private String description;
        private final List<ConnectorPropertyDescriptor> properties = new ArrayList<>();

        /**
         * Sets the name of the property sub-group.
         *
         * @param name the name of the sub-group
         * @return this Builder for method chaining
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description of the property sub-group.
         *
         * @param description the description of the sub-group
         * @return this Builder for method chaining
         */
        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * Adds a property to the sub-group.
         *
         * @param property the property to add
         * @return this Builder for method chaining
         */
        public Builder addProperty(final ConnectorPropertyDescriptor property) {
            if (property != null) {
                this.properties.add(property);
            }
            return this;
        }

        /**
         * Sets the properties for the sub-group, replacing any previously added properties.
         *
         * @param properties the properties to set
         * @return this Builder for method chaining
         */
        public Builder properties(final List<ConnectorPropertyDescriptor> properties) {
            this.properties.clear();
            if (properties != null) {
                this.properties.addAll(properties);
            }
            return this;
        }

        /**
         * Adds multiple properties to the sub-group.
         *
         * @param properties the properties to add
         * @return this Builder for method chaining
         */
        public Builder addProperties(final List<ConnectorPropertyDescriptor> properties) {
            if (properties != null) {
                this.properties.addAll(properties);
            }
            return this;
        }

        /**
         * Builds and returns a new ConnectorPropertySubGroup instance.
         *
         * @return a new ConnectorPropertySubGroup
         * @throws IllegalStateException if required fields are not set
         */
        public ConnectorPropertyGroup build() {
            if (description != null && (name == null || name.isBlank())) {
                throw new IllegalStateException("Property Group's name must be provided if a description is set");
            }

            // Ensure that all Property Descriptor names are unique within this group
            final Set<String> propertyNames = new HashSet<>();
            for (final ConnectorPropertyDescriptor property : properties) {
                if (!propertyNames.add(property.getName())) {
                    throw new IllegalStateException("All Property Descriptor names must be unique within a Property Group. Duplicate name found: " + property.getName());
                }
            }

            return new ConnectorPropertyGroup(this);
        }
    }
}
