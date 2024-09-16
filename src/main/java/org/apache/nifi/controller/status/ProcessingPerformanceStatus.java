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
package org.apache.nifi.controller.status;

public class ProcessingPerformanceStatus implements Cloneable {

    private String identifier;
    private long cpuDuration;
    private long contentReadDuration;
    private long contentWriteDuration;
    private long sessionCommitDuration;
    private long garbageCollectionDuration;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public long getCpuDuration() {
        return cpuDuration;
    }

    public void setCpuDuration(long cpuDuration) {
        this.cpuDuration = cpuDuration;
    }

    public long getContentReadDuration() {
        return contentReadDuration;
    }

    public void setContentReadDuration(long contentReadDuration) {
        this.contentReadDuration = contentReadDuration;
    }

    public long getContentWriteDuration() {
        return contentWriteDuration;
    }

    public void setContentWriteDuration(long contentWriteDuration) {
        this.contentWriteDuration = contentWriteDuration;
    }

    public long getSessionCommitDuration() {
        return sessionCommitDuration;
    }

    public void setSessionCommitDuration(long sessionCommitDuration) {
        this.sessionCommitDuration = sessionCommitDuration;
    }

    public long getGarbageCollectionDuration() {
        return garbageCollectionDuration;
    }

    public void setGarbageCollectionDuration(long garbageCollectionDuration) {
        this.garbageCollectionDuration = garbageCollectionDuration;
    }

    @Override
    public ProcessingPerformanceStatus clone() {
        final ProcessingPerformanceStatus clonedObj = new ProcessingPerformanceStatus();

        clonedObj.identifier = identifier;
        clonedObj.cpuDuration = cpuDuration;
        clonedObj.contentReadDuration = contentReadDuration;
        clonedObj.contentWriteDuration = contentWriteDuration;
        clonedObj.sessionCommitDuration = sessionCommitDuration;
        clonedObj.garbageCollectionDuration = garbageCollectionDuration;
        return clonedObj;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProcessorPerformanceStatus [Group ID= ");
        builder.append(identifier);
        builder.append(", cpuDuration= ");
        builder.append(cpuDuration);
        builder.append(", contentReadDuration= ");
        builder.append(contentReadDuration);
        builder.append(", contentWriteDuration= ");
        builder.append(contentWriteDuration);
        builder.append(", sessionCommitDuration= ");
        builder.append(sessionCommitDuration);
        builder.append(", garbageCollectionDuration= ");
        builder.append(garbageCollectionDuration);
        builder.append("]");
        return builder.toString();
    }
}
