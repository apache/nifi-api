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

package org.apache.nifi.components.connector;

import org.apache.nifi.components.Backlog;
import org.apache.nifi.components.BacklogReportingException;
import org.apache.nifi.components.connector.components.FlowContext;
import org.apache.nifi.components.connector.components.ProcessorFacade;

import java.util.Optional;

/**
 * <p>
 *     Optional capability interface implemented by {@link Connector}s that can report a
 *     {@link Backlog} describing how much data remains on the source system(s) the Connector
 *     pulls from but has not yet brought into NiFi. Implementing this interface is the opt-in
 *     signal: Connectors that do not implement it are simply not asked for backlog, and the
 *     framework's REST endpoints return HTTP {@code 409 Conflict} rather than treating "not
 *     supported" the same as "supported but currently unknown".
 * </p>
 *
 * <p>
 *     This mirrors the {@code BacklogReportingProcessor} capability interface on the Processor
 *     side: the {@code Connector} base interface stays focused on lifecycle and configuration,
 *     and backlog reporting is layered on as a separate opt-in.
 * </p>
 *
 * <p>
 *     Implementations that do report backlog typically derive the value from a single source —
 *     most often a single Processor's {@code BacklogReportingProcessor} report exposed via
 *     {@link ProcessorFacade#getBacklog()}. Some Connectors may compose a flow-wide picture from
 *     multiple sources: for example, summing the {@link Backlog} from multiple Processors, or, for
 *     a List/Fetch pattern, also inspecting queues and FlowFile attributes to account for data that
 *     has been "Listed" but not yet "Fetched".
 * </p>
 *
 * <h2>Return value semantics</h2>
 * <ul>
 *     <li>
 *         {@link Optional#empty()} means the Connector cannot report a {@link Backlog} right now,
 *         even though it implements this interface. This is distinct from a hard failure.
 *     </li>
 *     <li>
 *         <code>Optional.of(Backlog.caughtUp())</code> means the Connector is fully synchronized
 *         with its source as of now.
 *     </li>
 *     <li>
 *         <code>Optional.of(...)</code> with non-zero numeric dimensions means the Connector knows
 *         that this much data remains on the source. See {@link Backlog.Precision} for whether the
 *         numbers are exact or lower bounds.
 *     </li>
 * </ul>
 *
 * <p>
 *     Hard failures — source unreachable, authorization denied, state errors that prevent
 *     computation — are reported by throwing {@link BacklogReportingException}, not by returning
 *     {@link Optional#empty()}. This keeps failures distinguishable from "no value available."
 * </p>
 *
 * <p>
 *     <b>Implementation Note:</b> This API is currently experimental, as it is under very active
 *     development. As such, it is subject to change without notice between releases.
 * </p>
 */
public interface BacklogReportingConnector {

    /**
     * <p>
     *     Reports how much data remains on the source system(s) that this Connector pulls from but
     *     has not yet brought into NiFi.
     * </p>
     *
     * <p>
     *     This method is invoked against the <b>active</b> {@link FlowContext}, because backlog is
     *     a runtime property of the running flow. The working flow context is not used for backlog
     *     reporting. Expected call frequency is low — typically on the order of once per minute,
     *     sometimes once every few minutes. Implementations may cache results internally if computing
     *     backlog puts non-trivial load on the source, but caching is not required.
     * </p>
     *
     * @param activeFlowContext the active flow context against which to compute backlog
     * @return the Connector's reported {@link Backlog}, or {@link Optional#empty()} if the Connector
     *         cannot determine a value right now (for example, it has never polled the source)
     * @throws BacklogReportingException if the Connector attempted to determine its backlog and failed
     */
    Optional<Backlog> getBacklog(final FlowContext activeFlowContext) throws BacklogReportingException;
}
