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
 * Indicates whether a {@link Secret} may be referenced only by sensitive properties or may also
 * be referenced by non-sensitive properties.
 */
public enum PropertyProtectionType {

    /**
     * The secret may only be referenced by sensitive properties, that is, properties declared
     * with {@link PropertyType#SECRET}. This is the default classification.
     */
    RESTRICTED,

    /**
     * The owner of the secret has authorized it to be referenced by any property, including
     * non-sensitive properties.
     */
    UNRESTRICTED
}
