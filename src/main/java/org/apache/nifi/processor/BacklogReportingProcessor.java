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

package org.apache.nifi.processor;

import org.apache.nifi.components.Backlog;
import org.apache.nifi.components.BacklogReportingException;

import java.util.Optional;

/**
 * <p>
 *     Optional capability interface implemented by Processors that can report a {@link Backlog}
 *     describing how much data remains on the source system that the Processor has not yet pulled
 *     into NiFi. Implementing this interface is the opt-in signal: Processors that do not implement
 *     it are simply not asked for backlog.
 * </p>
 *
 * <p>
 *     The reported {@link Backlog} reflects only the data the Processor knows about from the source.
 *     A Connector that composes a flow-wide backlog generally also accounts for FlowFiles already
 *     enqueued in NiFi between processors; that composition is the Connector's responsibility, not
 *     the Processor's.
 * </p>
 *
 * <p>
 *     The expected call frequency for {@link #getBacklog(ProcessContext)} is low — on the order of
 *     once per minute, sometimes once every few minutes. Implementations may cache results internally
 *     if computing backlog puts non-trivial load on the source, but caching is not required.
 * </p>
 *
 * <h2>Return value semantics</h2>
 * <ul>
 *     <li>
 *         {@link Optional#empty()} means the Processor cannot report a {@code Backlog} right now,
 *         even though it implements this interface. This is distinct from a hard failure.
 *     </li>
 *     <li>
 *         <code>Optional.of(Backlog.caughtUp())</code> means the Processor is fully
 *         synchronized with its source as of now.
 *     </li>
 *     <li>
 *         <code>Optional.of(...)</code> with non-zero numeric dimensions means the Processor knows that
 *         this much data remains on the source. See {@link Backlog.Precision} for whether the
 *         numbers are exact or lower bounds.
 *     </li>
 * </ul>
 *
 * <p>
 *     Hard failures — source unreachable, authorization denied, state errors that prevent
 *     computation — are reported by throwing {@link BacklogReportingException}, not by returning
 *     {@link Optional#empty()}. This keeps failures distinguishable from "no value available."
 * </p>
 */
public interface BacklogReportingProcessor {

    /**
     * Returns the Processor's current view of how much data remains on the source.
     *
     * <p>
     *     The framework always supplies a non-null {@link ProcessContext}. Implementations may use
     *     the supplied context to read property values and to build whatever transient clients they
     *     need to query the source. Implementations are responsible for closing any transient
     *     clients they open during the call.
     * </p>
     *
     * <p>
     *     The framework does not serialize calls to this method. Multiple {@code getBacklog}
     *     invocations may run concurrently against the same Processor instance, so implementations
     *     must be safe for concurrent use. Implementations that open per-call transient clients are
     *     naturally isolated. Implementations that cache shared clients across calls must guard
     *     them appropriately or rely on thread-safe SDK clients.
     * </p>
     *
     * @param context a non-null {@link ProcessContext} usable to look up property values and to
     *         construct transient clients to the source
     * @return the Processor's reported {@link Backlog}, or {@link Optional#empty()} if the Processor
     *         cannot determine a value right now (e.g. it has never polled the source)
     * @throws BacklogReportingException if the Processor attempted to determine its backlog and
     *         failed due to a transient or permanent error such as I/O failure or authorization denial
     */
    Optional<Backlog> getBacklog(final ProcessContext context) throws BacklogReportingException;
}
