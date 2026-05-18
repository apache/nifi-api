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

import org.apache.nifi.components.Backlog;
import org.apache.nifi.components.BacklogReportingException;
import org.apache.nifi.components.ConfigVerificationResult;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.connector.InvocationFailedException;
import org.apache.nifi.flow.VersionedExternalFlow;
import org.apache.nifi.flow.VersionedParameterContext;
import org.apache.nifi.flow.VersionedProcessor;
import org.apache.nifi.processor.BacklogReportingProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 *     Facade exposing per-Processor operations to a Connector implementation. The framework constructs
 *     and supplies these facades; Connector extensions do not implement this interface themselves.
 * </p>
 */
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

    /**
     * Indicates whether the underlying Processor implements {@link BacklogReportingProcessor}. This
     * is a cheap capability check that does not call into the Processor.
     *
     * @return {@code true} if the underlying Processor implements {@link BacklogReportingProcessor};
     *         {@code false} otherwise
     */
    boolean reportsBacklog();

    /**
     * <p>
     *     Returns the underlying Processor's reported {@link Backlog}. This is the bridge a
     *     Connector uses to ask a Processor in its flow how much data remains on the source.
     * </p>
     *
     * <p>
     *     The return value and exception semantics mirror those of
     *     {@link BacklogReportingProcessor#getBacklog(org.apache.nifi.processor.ProcessContext)}. If
     *     the underlying Processor does not implement {@link BacklogReportingProcessor}, this method
     *     returns {@link Optional#empty()}.
     * </p>
     *
     * @return the Processor's reported {@link Backlog}, or {@link Optional#empty()} if the Processor does not
     *         implement {@link BacklogReportingProcessor} or has nothing to report
     * @throws BacklogReportingException if the Processor attempted to determine its backlog and failed
     */
    Optional<Backlog> getBacklog() throws BacklogReportingException;
}
