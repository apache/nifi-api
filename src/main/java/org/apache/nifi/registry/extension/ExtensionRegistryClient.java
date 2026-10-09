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
package org.apache.nifi.registry.extension;

import org.apache.nifi.components.ConfigurableComponent;
import org.apache.nifi.flow.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * <p>
 * Represents an external source from which NiFi extension bundles (NARs) can be
 * discovered, inspected, and retrieved at runtime. The interface allows multiple
 * types of registries to back NiFi (local filesystem, Maven repositories, cloud
 * storage, etc.).
 * </p>
 *
 * <p>
 * Implementations are packaged as NARs and configured via {@code nifi.properties}
 * using a prefix-based multi-provider convention. Each provider must:
 * </p>
 *
 * <ul>
 * <li>Implement this interface.</li>
 * <li>Provide a default (no-argument) constructor.</li>
 * </ul>
 *
 * <p>
 * After {@link #initialize(ExtensionRegistryClientInitializationContext)} is called,
 * implementations should expect incoming requests at any time. The request methods
 * receive an {@link ExtensionRegistryClientConfigurationContext} that always reflects
 * the current state of the configured properties. Caching properties between method
 * calls is not recommended.
 * </p>
 *
 * <p><b>Implementation Note:</b> This API is currently experimental, as it is under
 * very active development. As such, it is subject to change without notice between
 * minor releases.</p>
 */
public interface ExtensionRegistryClient extends ConfigurableComponent {

    /**
     * Initializes the extension registry client with the given context.
     *
     * @param context the context
     */
    void initialize(ExtensionRegistryClientInitializationContext context);

    /**
     * Returns the NAR file for the given bundle.
     *
     * @param context the context
     * @param bundle  the bundle
     * @return the NAR file
     * @throws ExtensionRegistryException if an error occurs
     * @throws IOException                if an error occurs
     */
    InputStream getNARFile(ExtensionRegistryClientConfigurationContext context, Bundle bundle) throws ExtensionRegistryException, IOException;

    /**
     * Returns the manifest file for the given bundle.
     *
     * @param context the context
     * @param bundle  the bundle
     * @return the manifest file
     * @throws ExtensionRegistryException if an error occurs
     * @throws IOException                if an error occurs
     */
    InputStream getManifestFile(ExtensionRegistryClientConfigurationContext context, Bundle bundle) throws ExtensionRegistryException, IOException;

    /**
     * Returns the list of bundles.
     *
     * @param context the context
     * @return the list of bundles
     * @throws ExtensionRegistryException if an error occurs
     * @throws IOException                if an error occurs
     */
    Set<Bundle> listBundles(ExtensionRegistryClientConfigurationContext context) throws ExtensionRegistryException, IOException;

    /**
     * Returns metadata for a bundle, including its dependency coordinate and extension manifest stream. This allows callers to
     * build dependency graphs without downloading the entire NAR separately.
     *
     * @param context the context
     * @param bundle  the bundle
     * @return metadata for the bundle
     * @throws ExtensionRegistryException if an error occurs
     * @throws IOException                if an error occurs
     */
    ExtensionBundleMetadata getBundleMetadata(ExtensionRegistryClientConfigurationContext context, Bundle bundle) throws ExtensionRegistryException, IOException;

}
