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

public class VersionedPropertyDescriptor {
    private String name;
    private String displayName;
    private boolean identifiesControllerService;
    private boolean sensitive;
    private boolean dynamic;
    private VersionedResourceDefinition resourceDefinition;

    @Schema(description = "The name of the property")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Schema(description = "The display name of the property")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Schema(description = "Whether or not the property provides the identifier of a Controller Service")
    public boolean getIdentifiesControllerService() {
        return identifiesControllerService;
    }

    public void setIdentifiesControllerService(boolean identifiesControllerService) {
        this.identifiesControllerService = identifiesControllerService;
    }

    @Schema(description = "Whether or not the property is considered sensitive")
    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    @Schema(description = "Whether or not the property is user-defined")
    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Schema(description = "Returns the Resource Definition that defines which type(s) of resource(s) this property references, if any")
    public VersionedResourceDefinition getResourceDefinition() {
        return resourceDefinition;
    }

    public void setResourceDefinition(final VersionedResourceDefinition resourceDefinition) {
        this.resourceDefinition = resourceDefinition;
    }
}
