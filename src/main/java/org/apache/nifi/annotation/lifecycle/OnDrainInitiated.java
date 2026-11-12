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

import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.ProcessContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Marker annotation a {@link org.apache.nifi.processor.Processor Processor} or
 * {@link org.apache.nifi.controller.ControllerService ControllerService} implementation
 * can use to indicate that a method should be called when a drain of its Process Group begins.
 * While a drain is in progress, no new data is brought into the group, so the component should
 * process the data that it already has rather than wait for more. For example, a Processor that
 * groups FlowFiles into bins might send its bins on sooner.
 * </p>
 *
 * <p>
 * The component may be in any lifecycle state when this method is called, and the order in which
 * this method is called relative to other lifecycle methods, such as those marked with
 * {@link OnScheduled}, {@link OnUnscheduled}, and {@link OnStopped}, is not guaranteed. It may be
 * called before the component is scheduled, while it is being scheduled, after it is scheduled, or
 * for a component that is not scheduled at all during the drain. It may also be called while other
 * threads are running the {@code onTrigger} method. The method must therefore be thread-safe, must
 * not depend on anything created by a method marked with {@link OnScheduled}, and should do little
 * more than take note that a drain has begun, for example by setting a flag.
 * </p>
 *
 * <p>
 * This method is called at most once for each drain. When the drain ends, methods marked with
 * {@link OnDrainCompleted} are called. Because the component may be stopped and started again
 * while the drain is in progress, anything noted when the drain began should be cleared only by
 * a method marked with {@link OnDrainCompleted}, not by a method marked with {@link OnScheduled}.
 * </p>
 *
 * <p>
 * Methods with this annotation are permitted to take either 0 or 1 argument. If an argument is
 * used, it must be of type {@link ProcessContext} if the component is a Processor or of type
 * {@link ConfigurationContext} if the component is a Controller Service.
 * </p>
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OnDrainInitiated {

}
