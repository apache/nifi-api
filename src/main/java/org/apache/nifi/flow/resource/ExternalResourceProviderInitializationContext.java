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

import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Contains necessary information for extensions of external resource provider functionality.
 */
public interface ExternalResourceProviderInitializationContext {

    /**
     * @return Returns with the available properties.
     */
    Map<String, String> getProperties();

    /**
     * @return A predicate, which might filter out unwanted files from the external source during listing. If no filtering
     * necessary, then the predicate should return {@code true} for every call.
     */
    default Predicate<ExternalResourceDescriptor> getFilter() {
        return (descriptor) -> true;
    }

    /**
     * @return Returns an SSLContext.
     */
    SSLContext getSSLContext();
}
