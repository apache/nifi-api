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

import java.util.List;

/**
 * Represents a dynamically discoverable ingress port provided by a {@link ListenComponent}.
 */
public interface ListenPort {

    /**
     * Get the operating system numbered port that is listening for network traffic.
     *
     * @return the port number
     */
    int getPortNumber();

    /**
     * Get the name of the listen port.
     *
     * @return A descriptive name of the listen port. Useful for {@link ListenComponent}s that provide more than one port.
     */
    String getPortName();

    /**
     * Get the layer 4 transport protocol that is used at the OS networking level for this port.
     *
     * @return the transport protocol
     */
    TransportProtocol getTransportProtocol();

    /**
     * Get the currently configured application protocols that this port supports.
     * <p>
     *   Note that this is not always the same as the application protocols that could be supported. For example, if this port could support http/1.1 or h2 (HTTP 2),
     *   but is currently configured to require h2, then this method should return [h2], not [http1.1, h2].
     * </p>
     * <p>
     *   See {@link ListenPortDefinition#getApplicationProtocols()} for guidance on application protocol string values.
     *   This method should return a subset of application protocol values specified by the corresponding PropertyDescriptor {@link ListenPortDefinition}.
     * </p>
     *
     * @return the application protocols supported by this listen port, if applicable; otherwise an empty list.
     */
    List<String> getApplicationProtocols();

}
