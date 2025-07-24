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
import org.apache.nifi.flow.VersionedControllerService;
import org.apache.nifi.flow.VersionedExternalFlow;
import org.apache.nifi.flow.VersionedParameterContext;

import java.util.List;
import java.util.Map;

public interface ControllerServiceFacade {

    VersionedControllerService getDefinition();

    ControllerServiceLifecycle getLifecycle();

    List<ValidationResult> validate();

    List<ValidationResult> validate(Map<String, String> propertyValues);

    List<ConfigVerificationResult> verify(Map<String, String> propertyValues, Map<String, String> variables);

    List<ConfigVerificationResult> verify(Map<String, String> propertyValues, VersionedParameterContext parameterContext, Map<String, String> variables);

    List<ConfigVerificationResult> verify(VersionedExternalFlow versionedExternalFlow, Map<String, String> variables);

    Object invokeConnectorMethod(String methodName, Map<String, Object> arguments) throws InvocationFailedException;

    <T> T invokeConnectorMethod(String methodName, Map<String, Object> arguments, Class<T> returnType) throws InvocationFailedException;

}
