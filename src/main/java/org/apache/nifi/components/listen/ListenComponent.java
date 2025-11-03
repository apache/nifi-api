/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.components.listen;

import org.apache.nifi.controller.ConfigurationContext;

import java.util.List;

/**
 * An extension component (e.g., Processor or ControllerService) that creates one or more ingress network ports.
 * <p>
 *   Implementing this interface allows {@link ListenPort}s provided by this component to be dynamically discoverable by the framework.
 * </p>
 * <p>
 *   Typically, components implementing this interface should have at least one property described using a {@link org.apache.nifi.components.PropertyDescriptor}
 *   that identifies a {@link ListenPortDefinition}. The Property Descriptor identifies a possible Listen Port that could be created.
 *   This interface provides actual the ports configured based on component property values, along with additional ingress metadata.
 * </p>
 */
public interface ListenComponent {

    /**
     * A list of listen ports provided by this component based on its current configuration.
     *
     * @param context provides access to convenience methods for obtaining property values
     * @return a list of zero or more listen ports that are actively configured to be provided by this component.
     */
    List<ListenPort> getListenPorts(final ConfigurationContext context);
}
