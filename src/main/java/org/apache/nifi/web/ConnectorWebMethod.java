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
package org.apache.nifi.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method in a Connector interface requires specific authorization to be invoked.
 * This annotation is used by the NiFi framework to enforce access control when custom UIs interact
 * with Connector instances.
 *
 * <p>Methods without this annotation will be blocked by the framework, preventing their invocation
 * through the Connector web context.</p>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {@code
 * import static org.apache.nifi.web.ConnectorWebMethod.AccessType;
 *
 * public interface MyConnector {
 *     @ConnectorWebMethod(AccessType.READ)
 *     List<String> getAvailableOptions();
 *
 *     @ConnectorWebMethod(AccessType.WRITE)
 *     void applyConfiguration(Map<String, String> config);
 *
 *     // This method cannot be called through the web context - throws IllegalStateException
 *     void internalMethod();
 * }
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConnectorWebMethod {
    /**
     * The type of access required to invoke this method.
     * READ for read-only operations, WRITE for operations that modify state.
     * @return the required access type
     */
    AccessType value();

    /**
     * Defines the type of access required for a connector method.
     */
    enum AccessType {
        /**
         * Indicates the method only reads data and does not modify state.
         */
        READ,

        /**
         * Indicates the method modifies state.
         */
        WRITE
    }
}

