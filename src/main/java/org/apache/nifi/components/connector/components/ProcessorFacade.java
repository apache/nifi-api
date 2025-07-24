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

package org.apache.nifi.components.connector.components;

import org.apache.nifi.components.ConfigVerificationResult;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.connector.InvocationFailedException;
import org.apache.nifi.flow.VersionedExternalFlow;
import org.apache.nifi.flow.VersionedParameterContext;
import org.apache.nifi.flow.VersionedProcessor;

import java.util.List;
import java.util.Map;

public interface ProcessorFacade {

    VersionedProcessor getDefinition();

    ProcessorLifecycle getLifecycle();

    List<ValidationResult> validate();

    List<ValidationResult> validate(Map<String, String> propertyValues);

    List<ConfigVerificationResult> verify(Map<String, String> propertyValues, Map<String, String> attributes);

    List<ConfigVerificationResult> verify(Map<String, String> propertyValues, VersionedParameterContext parameterContext, Map<String, String> attributes);

    List<ConfigVerificationResult> verify(VersionedExternalFlow versionedExternalFlow, Map<String, String> attributes);

    /**
     * <p>
     * Invokes the {@link ConnectorMethod} with the given name, passing in the provided arguments. The arguments Map will be
     * serialized into JSON. This provides the ability to pass complex data structures but means that arbitrary objects that
     * contain methods will not be provided as-is. This is necessary due to ClassLoader isolation.
     * </p>
     * <p>
     * Likewise, the return value will be deserialized from JSON into a standard Java object. Depending on the value returned,
     * the returned object may be a primitive, a String, List, Map, etc. Complex objects will be represented as Maps of property names to values.
     * </p>
     *
     * @param methodName the name of the ConnectorMethod to invoke
     * @param arguments the arguments to pass to the method
     * @return the result of the method invocation, deserialized from JSON
     * @throws InvocationFailedException if unable to invoke the method
     */
    Object invokeConnectorMethod(String methodName, Map<String, Object> arguments) throws InvocationFailedException;

    /**
     * Invokes the {@link ConnectorMethod} with the given name, passing in the provided arguments. The arguments
     * Map will be serialized into JSON. This provides the ability to pass complex data structures but means that
     * arbitrary objects that contain methods will not be provided as-is. This is necessary due to ClassLoader
     * isolation.
     *
     * @param methodName the name of the ConnectorMethod to invoke
     * @param arguments the arguments to pass to the method
     * @param returnType the expected return type
     * @return the result of the method invocation, deserialized from JSON into the specified return type
     * @param <T> the expected return type
     * @throws InvocationFailedException if unable to invoke the method
     */
    <T> T invokeConnectorMethod(String methodName, Map<String, Object> arguments, Class<T> returnType) throws InvocationFailedException;
}
