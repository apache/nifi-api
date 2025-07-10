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

package org.apache.nifi.registry.flow;

import org.apache.nifi.components.ConfigVerificationResult;
import org.apache.nifi.logging.ComponentLog;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Any Flow Registry Client that implements this interface will be provided the opportunity to verify
 *     a given configuration of the Flow Registry Client. This allows the Flow Registry Client to provide meaningful feedback
 *     to users when configuring the dataflow.
 * </p>
 *
 * <p>
 *     Generally speaking, verification differs from validation in that validation is expected to be very
 *     quick and run often. If a Flow Registry Client is not valid, it cannot be started. However, verification may be
 *     more expensive or time-consuming to complete. For example, validation may ensure that a username is
 *     provided for connecting to an external service but should not perform any sort of network connection
 *     in order to verify that the username is accurate. Verification, on the other hand, may create resources
 *     such as network connections, may be more expensive to complete, and may be run only when a user invokes
 *     the action (though verification may later occur at other stages, such as when starting a component).
 * </p>
 *
 * <p>
 *     Verification is allowed to be run only when a Flow Registry Client is fully disabled.
 *     Therefore, any initialization logic that may need to be performed
 *     before the Flow Registry Client is triggered may also be required for verification. However, the framework is not responsible
 *     for triggering the Lifecycle management stages before triggering the verification. Such
 *     methods should be handled by the {@link #verify(FlowRegistryClientConfigurationContext, ComponentLog, Map)} itself.
 *     The {@link #verify(FlowRegistryClientConfigurationContext, ComponentLog, Map)} method will only be called if the configuration is valid according to the
 *     validation rules (i.e., all Property Descriptors' validators and customValidate methods have indicated that the configuration is valid).
 * </p>
 */
public interface VerifiableFlowRegistryClient {

    /**
     * Verifies that the configuration defined by the given FlowRegistryClientConfigurationContext is valid.
     * @param context the FlowRegistryClientConfigurationContext that contains the necessary configuration
     * @param verificationLogger a logger that can be used during verification. While the typical logger can be used, doing so may result
     * in producing bulletins, which can be confusing.
     * @param variables a Map of key/value pairs that can be used to resolve variables referenced in property values via Expression Language
     *
     * @return a List of ConfigVerificationResults, each illustrating one step of the verification process that was completed
     */
    List<ConfigVerificationResult> verify(FlowRegistryClientConfigurationContext context, ComponentLog verificationLogger, Map<String, String> variables);

}
