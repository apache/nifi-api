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
package org.apache.nifi.controller;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.context.PropertyContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This context is passed to ControllerServices and Reporting Tasks in order
 * to expose their configuration to them.
 */
public interface ConfigurationContext extends PropertyContext {

    /**
     * @return an unmodifiable map of all configured properties for this
     * {@link ControllerService}
     */
    Map<PropertyDescriptor, String> getProperties();

    /**
     * @return the annotation data configured for the component
     */
    String getAnnotationData();

    /**
     * @return a String representation of the scheduling period, or <code>null</code> if
     *         the component does not have a scheduling period (e.g., for ControllerServices)
     */
    String getSchedulingPeriod();

    /**
     * Returns the amount of time, in the given {@link TimeUnit} that will
     * elapsed between the return of one execution of the
     * component's <code>onTrigger</code> method and
     * the time at which the method is invoked again. This method will return
     * null if the component does not have a scheduling period (e.g., for ControllerServices)
     *
     * @param timeUnit unit of time for scheduling
     * @return period of time or <code>null</code> if component does not have a scheduling
     *         period
     */
    Long getSchedulingPeriod(TimeUnit timeUnit);

    /**
     * Returns the component's (ControllerService, ReportingTask, ParameterProvider, e.g.) name
     * @return the String name of this component
     */
    String getName();
}
