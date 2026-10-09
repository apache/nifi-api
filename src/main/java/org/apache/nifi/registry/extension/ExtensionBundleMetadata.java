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

import org.apache.nifi.flow.Bundle;

import java.io.InputStream;

/**
 * Metadata about a remotely available bundle, including its dependency coordinate and manifest contents.
 * Implementations may choose to lazily stream the manifest InputStream; callers are responsible for closing it.
 */
public interface ExtensionBundleMetadata {

    /**
     * @return the bundle coordinates (including dependency if populated)
     */
    Bundle getBundle();

    /**
     * @return InputStream for the extension manifest (META-INF/docs/extension-manifest.xml). Caller must close.
     */
    InputStream getExtensionManifest();
}
