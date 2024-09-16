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

import java.util.Arrays;

/**
 * Enumeration of permission required for annotated components
 */
public enum RequiredPermission {
    READ_FILESYSTEM("read-filesystem", "read filesystem"),
    WRITE_FILESYSTEM("write-filesystem", "write filesystem"),
    READ_DISTRIBUTED_FILESYSTEM("read-distributed-filesystem", "read distributed filesystem"),
    WRITE_DISTRIBUTED_FILESYSTEM("write-distributed-filesystem", "write distributed filesystem"),
    EXECUTE_CODE("execute-code", "execute code"),
    ACCESS_KEYTAB("access-keytab", "access keytab"),
    ACCESS_TICKET_CACHE("access-ticket-cache", "access ticket cache"),
    ACCESS_ENVIRONMENT_CREDENTIALS("access-environment-credentials", "access environment credentials"),
    EXPORT_NIFI_DETAILS("export-nifi-details", "export nifi details"),
    REFERENCE_REMOTE_RESOURCES("reference-remote-resources", "reference remote resources");

    private String permissionIdentifier;
    private String permissionLabel;

    RequiredPermission(String permissionIdentifier, String permissionLabel) {
        this.permissionIdentifier = permissionIdentifier;
        this.permissionLabel = permissionLabel;
    }

    public String getPermissionIdentifier() {
        return permissionIdentifier;
    }

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public static RequiredPermission valueOfPermissionIdentifier(final String permissionIdentifier) {
        return Arrays.stream(values())
                .filter(candidate -> candidate.getPermissionIdentifier().equals(permissionIdentifier))
                .findFirst().orElse(null);
    }
}
