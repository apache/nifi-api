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
package org.apache.nifi.annotation.lifecycle;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.ProcessContext;

/**
 * <p>
 * Marker annotation a {@link org.apache.nifi.processor.Processor Processor},
 * {@link org.apache.nifi.controller.ControllerService ControllerService},
 * {@link org.apache.nifi.registry.flow.FlowRegistryClient FlowRegistryClient},
 * {@link org.apache.nifi.parameter.ParameterProvider ParameterProvider},
 * {@link org.apache.nifi.flowanalysis.FlowAnalysisRule FlowAnalysisRule}, or
 * {@link org.apache.nifi.reporting.ReportingTask ReportingTask} implementation
 * can use to indicate a method should be called whenever the component is
 * removed from the flow. This method will be called once for the entire life of
 * a component instance. If the method throw any Throwable, that Throwable will
 * be caught and logged but will not prevent subsequent methods with this
 * annotation or removal of the component from the flow.
 * </p>
 *
 * <p>
 * Methods with this annotation are permitted to take no arguments or to take a
 * single argument. If using a single argument, that argument must be of type
 * {@link ConfigurationContext} if the component is a ReportingTask, a ParameterProvider, or a
 * ControllerService. If the component is a Processor, then the argument must be
 * of type {@link ProcessContext}.
 * </p>
 *
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OnRemoved {
}
