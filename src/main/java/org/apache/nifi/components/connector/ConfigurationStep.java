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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConfigurationStep {
    private final String name;
    private final String description;
    private final List<ConnectorPropertyGroup> propertyGroups;

    private ConfigurationStep(final Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.propertyGroups = Collections.unmodifiableList(builder.propertyGroups);
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

    public static final class Builder {
        private String name;
        private String description;
        private List<ConnectorPropertyGroup> propertyGroups = Collections.emptyList();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder propertyGroups(final List<ConnectorPropertyGroup> propertyGroups) {
            this.propertyGroups = new ArrayList<>(propertyGroups);
            return this;
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
