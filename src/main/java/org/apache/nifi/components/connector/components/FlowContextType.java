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

public enum FlowContextType {

    /**
     * The ACTIVE Flow Context represents the version of the flow that able to be started and stopped and run
     * the dataflow.
     */
    ACTIVE,

    /**
     * The WORKING Flow Context represents the "in process" version of the flow that is being configured. This
     * version of the flow is not directly started or stopped by the user, but is used to verify configuration,
     * fetch allowable values, and perform other configuration-related operations.
     */
    WORKING;
}
