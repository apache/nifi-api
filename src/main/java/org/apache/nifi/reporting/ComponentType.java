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
package org.apache.nifi.reporting;

/**
 * An Enumeration for indicating which type of component a Bulletin is associated with
 */
public enum ComponentType {

    /**
     * Bulletin is associated with a Processor
     */
    PROCESSOR,

    /**
     * Bulletin is associated with a Remote Process Group
     */
    REMOTE_PROCESS_GROUP,

    /**
     * Bulletin is associated with an Input Port
     */
    INPUT_PORT,

    /**
     * Bulletin is associated with an Output Port
     */
    OUTPUT_PORT,

    /**
     * Bulletin is associated with a Reporting Task
     */
    REPORTING_TASK,

    /**
     * Bulletin is associated with a Flow Analysis Rule
     */
    FLOW_ANALYSIS_RULE,

    /**
     * Bulletin is associated with a Process Group
     */
    PROCESS_GROUP,

    /**
     * Bulletin is associated with a Parameter Provider
     */
    PARAMETER_PROVIDER,

    /**
     * Bulletin is associated with a Controller Service
     */
    CONTROLLER_SERVICE,

    /**
     * Bulletin is a system-level bulletin, associated with the Flow Controller
     */
    FLOW_CONTROLLER,

    /**
     * Bulletin is associated with a Flow Registry Client
     */
    FLOW_REGISTRY_CLIENT;
}
