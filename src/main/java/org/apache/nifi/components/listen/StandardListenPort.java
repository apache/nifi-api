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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StandardListenPort implements ListenPort {

    private final String portName;
    private final int portNumber;
    private final TransportProtocol transportProtocol;
    private final List<String> applicationProtocols;

    private StandardListenPort(final Builder builder) {
        Objects.requireNonNull(builder.portName, "Port name is required");
        Objects.requireNonNull(builder.transportProtocol, "Transport protocol is required");
        Objects.requireNonNull(builder.applicationProtocols, "Application protocols is required. Use empty list if there are no application protocols.");

        this.portName = builder.portName;
        this.portNumber = builder.portNumber;
        this.transportProtocol = builder.transportProtocol;
        this.applicationProtocols = builder.applicationProtocols;
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public String getPortName() {
        return portName;
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
        return "StandardListenPort[portName=%s, portNumber=%s, transportProtocol=%s, applicationProtocols=%s]".formatted(portName, portNumber, transportProtocol, applicationProtocols);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StandardListenPort that = (StandardListenPort) o;
        return portNumber == that.portNumber
            && Objects.equals(portName, that.portName)
            && transportProtocol == that.transportProtocol
            && Objects.equals(applicationProtocols, that.applicationProtocols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(portName, portNumber, transportProtocol, applicationProtocols);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String portName;
        private int portNumber;
        private TransportProtocol transportProtocol;
        private List<String> applicationProtocols = Collections.emptyList();

        public Builder portNumber(final int portNumber) {
            this.portNumber = portNumber;
            return this;
        }

        public Builder portName(final String portName) {
            this.portName = portName;
            return this;
        }

        public Builder transportProtocol(final TransportProtocol transportProtocol) {
            this.transportProtocol = transportProtocol;
            return this;
        }

        public Builder applicationProtocols(final List<String> applicationProtocols) {
            this.applicationProtocols = applicationProtocols != null ? applicationProtocols : Collections.emptyList();
            return this;
        }

        public StandardListenPort build() {
            return new StandardListenPort(this);
        }
    }

}
