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

package org.apache.nifi.components;

/**
 * <p>
 *     Thrown by a Connector or Processor that supports backlog reporting but cannot
 *     determine its current backlog due to a transient or permanent failure. Typical causes
 *     include I/O errors reaching the source system, authorization failures (the component is
 *     allowed to consume data but is not authorized to call the offset or listing API), and
 *     state errors that prevent computation.
 * </p>
 *
 * <p>
 *     This exception is distinct from a Connector or Processor not supporting backlog reporting
 *     at all. Components that do not support backlog reporting simply do not implement
 *     {@link org.apache.nifi.processor.BacklogReportingProcessor} or
 *     {@link org.apache.nifi.components.connector.BacklogReportingConnector}. Components that do
 *     support backlog reporting may return {@link java.util.Optional#empty()} when they cannot
 *     determine a value right now, or throw this exception when an attempt to determine the
 *     backlog fails. Callers should surface the cause rather than silently treat the failure as
 *     "not supported".
 * </p>
 */
public class BacklogReportingException extends Exception {

    public BacklogReportingException(final String message) {
        super(message);
    }

    public BacklogReportingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BacklogReportingException(final Throwable cause) {
        super(cause);
    }
}
