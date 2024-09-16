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

package org.apache.nifi.flow;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public class VersionedParameterProvider extends VersionedComponent implements VersionedConfigurableComponent, VersionedExtensionComponent {
    private String type;
    private Bundle bundle;
    private Map<String, String> properties;
    private Map<String, VersionedPropertyDescriptor> propertyDescriptors;
    private String annotationData;


    @Override
    @Schema(description = "The type of the parameter provider.")
    public String getType() {
        return type;
    }

    @Override
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    @Schema(description = "The details of the artifact that bundled this parameter provider type.")
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    @Schema(description = "The properties of the parameter provider.")
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    @Schema(description = "The property descriptors for the parameter provider.")
    public Map<String, VersionedPropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }

    @Override
    public void setPropertyDescriptors(Map<String, VersionedPropertyDescriptor> propertyDescriptors) {
        this.propertyDescriptors = propertyDescriptors;
    }

    @Schema(description = "The annotation for the parameter provider. This is how the custom UI relays configuration to the parameter provider.")
    public String getAnnotationData() {
        return annotationData;
    }

    public void setAnnotationData(String annotationData) {
        this.annotationData = annotationData;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.PARAMETER_PROVIDER;
    }
}
