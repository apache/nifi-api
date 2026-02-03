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

/**
 * A summary of FlowFiles that were dropped from a FlowFile Queue.
 * This class provides information about the number of FlowFiles dropped
 * and the total aggregate size in bytes of those FlowFiles.
 */
public class DropFlowFileSummary {

    private final int droppedCount;
    private final long droppedBytes;

    /**
     * Creates a new DropFlowFileSummary with the given count and byte size.
     *
     * @param droppedCount the number of FlowFiles that were dropped
     * @param droppedBytes the total size in bytes of all dropped FlowFiles
     */
    public DropFlowFileSummary(final int droppedCount, final long droppedBytes) {
        this.droppedCount = droppedCount;
        this.droppedBytes = droppedBytes;
    }

    /**
     * @return the number of FlowFiles that were dropped
     */
    public int getDroppedCount() {
        return droppedCount;
    }

    /**
     * @return the total size in bytes of all dropped FlowFiles
     */
    public long getDroppedBytes() {
        return droppedBytes;
    }

    /**
     * Creates a new DropFlowFileSummary that represents the combination of this summary and the given summary.
     *
     * @param other the other summary to add to this one
     * @return a new DropFlowFileSummary representing the combined totals
     */
    public DropFlowFileSummary add(final DropFlowFileSummary other) {
        return new DropFlowFileSummary(this.droppedCount + other.droppedCount, this.droppedBytes + other.droppedBytes);
    }

    @Override
    public String toString() {
        return "DropFlowFileSummary[droppedCount=" + droppedCount + ", droppedBytes=" + droppedBytes + "]";
    }
}
