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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation that can be added to a method in a Processor or ControllerService in order
 * to expose the method to connectors for invocation. The method must be public and
 * not static. The method may return a value. However, the value that is returned will
 * be converted into a JSON object and that JSON object will be returned to the caller.
 * </p>
 *
 * <p>
 * The following example shows a method that is exposed to connectors:
 * </p>
 *
 <pre>
 * {@code
 * @ConnectorMethod(
 *     name = "echo",
 *     description = "Returns the provided text after concatenating it the specified number of times.",
 *     allowedStates = {ComponentState.STOPPED, ComponentState.STOPPING, ComponentState.STARTING, ComponentState.RUNNING},
 *     arguments = {
 *         @MethodArgument(name = "text", type = String.class, description = "The text to echo", required = true),
 *         @MethodArgument(name = "iterations", type = int.class, description = "The number of iterations to echo the text", required = false)
 *     }
 * )
 * public String echo(Map<String, Object> arguments) {
 *     final StringBuilder sb = new StringBuilder();
 *     final String text = (String) arguments.get("text");
 *     final int iterations = (int) arguments.getOrDefault("iterations", 2);
 *     for (int i = 0; i < iterations; i++) {
 *       sb.append(text);
 *
 *       if (i < (iterations - 1)) {
 *         sb.append("\n");
 *       }
 *     }
 *
 *     return sb.toString();
 * }
 * }
 * </pre>
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ConnectorMethod {
    /**
     * The name of the method as it will be exposed to connectors.
     * @return the method name
     */
    String name();

    /**
     * A description of the method
     * @return the method description
     */
    String description() default "";

    /**
     * The states in which the component that defines the method is allowed to be in
     * when the method is invoked. If the Processor or ControllerService is not in one of these states,
     * any attempt to invoke the method will result in an error. The default states include all but PROCESSOR_DISABLED.
     *
     * @return the states in which the component that defines the method is allowed to be in when the method is invoked
     */
    ComponentState[] allowedStates() default {
        ComponentState.STOPPED,
        ComponentState.STOPPING,
        ComponentState.STARTING,
        ComponentState.RUNNING
    };

    /**
     * The arguments that the method accepts. Each argument is described by a MethodArgument annotation.
     * If no arguments are required, this can be left empty.
     *
     * @return the method arguments
     */
    MethodArgument[] arguments() default {};
}
