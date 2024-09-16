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

/**
 * <p>
 * Marker annotation a {@link org.apache.nifi.processor.Processor Processor},
 * {@link org.apache.nifi.controller.ControllerService ControllerService},
 * {@link org.apache.nifi.registry.flow.FlowRegistryClient FlowRegistryClient},
 * {@link org.apache.nifi.parameter.ParameterProvider ParameterProvider},
 * {@link org.apache.nifi.flowanalysis.FlowAnalysisRule FlowAnalysisRule}, or
 * {@link org.apache.nifi.reporting.ReportingTask ReportingTask} implementation
 * can use to indicate a method should be called whenever the component is added
 * to the flow. This method will be called once for the entire life of a
 * component instance.
 * </p>
 *
 * <p>
 * Methods with this annotation are called without any arguments, as all
 * settings and properties can be assumed to be the defaults.
 * </p>
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OnAdded {
}
