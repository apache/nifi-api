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
 * Defines the number and types of resources that allowed to be referenced by a component property
 */
public interface ListenPortDefinition {

    /**
     * Specifies the transport protocol that is used for communication with this listen port.
     *
     * @return the {@link TransportProtocol} enum value
     */
    TransportProtocol getTransportProtocol();

    /**
     * Specifies zero, one, or many application protocols that could be supported on this listen port.
     * <p>
     * This is used as a hint for NiFi runtimes and environments to offer richer behavior (such as configuration or validation) for application protocols they understand.
     * If more than one application protocol could be supported, but is decided at runtime based on configuration, this method should return all possible application protocols.
     * Inspecting the component with a Listen Port at runtime can determine more details about what has been configured.
     * <p>
     * General guidance for application protocol string values:
     * <ol>
     *     <li>
     *       Use IANA names when possible. For example:
     *        <p>
     *        <a href="https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml">
     *          IANA Service Name and Transport Protocol Port Number Registry
     *        </a>
     *        <p>
     *        <a href="https://www.iana.org/assignments/tls-extensiontype-values/tls-extensiontype-values.xhtml#alpn-protocol-ids">
     *          IANA TLS Application-Layer Protocol Negotiation (ALPN) Protocol IDs
     *        </a>
     *        <p>
     *        <a href="https://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml">
     *          IANA Uniform Resource Identifier (URI) Schemes
     *        </a>
     *     </li>
     *     <li>
     *       Do not include TLS variants of protocols. NiFi Listen Processors generally support TLS when possible, and the SSLContextProvider configuration is enough information to infer if the app
     *      protocol is using TLS. For example, there is no need to include wss for Websocket over TLS or h2c for HTTP/2 over TCP without TLS.
     *     </li>
     *     <li>
     *       For application protocols built on HTTP, such as gPRC, use or include the foundational HTTP protocol(s) in the application protocol list for the ListenPortDefinition.
     *       Protocols built on HTTP usually are just specifications for structuring data payloads within HTTP requests, but the HTTP request semantics are likely the most important aspect for system
     *       components that will be discovering NiFi Listen Ports, such as ingress controllers, load balancers, gateways, proxies, etc. Data payload structure is usually only important to a NiFi
     *      component, not networking components external to NiFi. You may also include application protocol(s) layered atop HTTP that are relevant to the Listen Port, if applicable.
     *      For example: ["http/1.1", "h2", "grpc"]
     *     </li>
     * </ol>
     *
     * @return one or more application protocols that could be supported by the processor,
     * or an empty list if no application protocols are known to be supported.
     */
    List<String> getApplicationProtocols();

}
