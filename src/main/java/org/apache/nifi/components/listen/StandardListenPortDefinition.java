/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.components.listen;

import org.apache.nifi.components.PropertyDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Listen Port definition for a {@link PropertyDescriptor} that specifies a listen port
 */
public class StandardListenPortDefinition implements ListenPortDefinition {

    private final TransportProtocol transportProtocol;
    private final List<String> applicationProtocols;

    /**
     * Create a {@link ListenPortDefinition}.
     *
     * @param transportProtocol - the layer 4 transport protocol used by the listen port
     * @param applicationProtocols - application protocols supported by the listen port or empty list
     */
    public StandardListenPortDefinition(TransportProtocol transportProtocol, List<String> applicationProtocols) {
        Objects.requireNonNull(transportProtocol, "Transport protocol is required.");
        Objects.requireNonNull(applicationProtocols, "Application protocols or empty list is required.");

        this.transportProtocol = transportProtocol;
        this.applicationProtocols = applicationProtocols;
    }

    /**
     * Create a {@link ListenPortDefinition} without any application protocols.
     *
     * @param transportProtocol - the layer 4 transport protocol used by the listen port
     */
    public StandardListenPortDefinition(final TransportProtocol transportProtocol) {
        this(transportProtocol, Collections.emptyList());
    }

    @Override
    public TransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    @Override
    public List<String> getApplicationProtocols() {
        return applicationProtocols;
    }

    @Override
    public String toString() {
        return "StandardListenPortDefinition[transportProtocol=%s, applicationProtocols=%s]".formatted(transportProtocol, applicationProtocols);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StandardListenPortDefinition that = (StandardListenPortDefinition) o;
        return transportProtocol == that.transportProtocol && Objects.equals(applicationProtocols, that.applicationProtocols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transportProtocol, applicationProtocols);
    }
}
