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

public enum ComponentState {
    /**
     * State in which a Processor is disabled. Note that Controller Services' notions of Disabled and Enabled
     * are different than Processors' notions of Disabled and Enabled. For Controller Services,
     * Component States of STOPPED, RUNNING are mapped to Controller Services' DISABLED, ENABLED states respectively.
     */
    PROCESSOR_DISABLED,

    /**
     * Processor is stopped or Controller Service is disabled
     */
    STOPPED,

    /**
     * Processor is starting or Controller Service is enabling
     */
    STARTING,

    /**
     * Processor is running or Controller Service is enabled
     */
    RUNNING,

    /**
     * Processor is stopping or Controller Service is disabling
     */
    STOPPING;
}
