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
package org.apache.nifi.flow.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Represents an external source where the resource files might be acquired from. These external resources might be
 * various: database drivers, different kind of configurations and so on.
 */
public interface ExternalResourceProvider {

    /**
     * Initializes the External Resource Provider based on the given set of properties.
     *
     * @param context Initialization context for the provider containing configuration properties
     */
    void initialize(ExternalResourceProviderInitializationContext context);

    /**
     * Performs a listing of all resources that are available.
     *
     * @return The result is a list of descriptors for the available resources.
     * @throws IOException Thrown on failure to list resources
     */
    Collection<ExternalResourceDescriptor> listResources() throws IOException;

    /**
     * Fetches the resource determined by the descriptor. This usually happens based on {@code ExternalResourceDescriptor#getLocation}
     * but implementations might differ.
     *
     * @param descriptor External Resource reference to be fetched
     * @return Input Stream for external resource fetched
     * @throws IOException Thrown on failure to fetch resources
     */
    InputStream fetchExternalResource(ExternalResourceDescriptor descriptor) throws IOException;
}
