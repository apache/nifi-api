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

import java.util.Objects;

/**
 * A ConnectorValueReference implementation representing a reference to a secret.
 */
public final class SecretReference implements ConnectorValueReference {

    private final String providerId;
    private final String providerName;
    private final String secretName;
    private final String fullyQualifiedName;

    public SecretReference(final String providerId, final String providerName, final String secretName, final String fullyQualifiedName) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.secretName = secretName;
        this.fullyQualifiedName = fullyQualifiedName;
    }

    /**
     * Returns the identifier of the secret provider.
     *
     * @return the provider identifier
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Returns the name of the secret provider.
     *
     * @return the provider name
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Returns the simple secret name.
     *
     * @return the simple secret name
     */
    public String getSecretName() {
        return secretName;
    }

    /**
     * Returns the fully qualified name of the secret.
     * @return the fully qualified name
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public ConnectorValueType getValueType() {
        return ConnectorValueType.SECRET_REFERENCE;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final SecretReference that = (SecretReference) object;
        return Objects.equals(providerId, that.providerId) && Objects.equals(providerName, that.providerName)
               && Objects.equals(secretName, that.secretName) && Objects.equals(fullyQualifiedName, that.fullyQualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, providerName, secretName, fullyQualifiedName);
    }

    @Override
    public String toString() {
        return "SecretReference[providerId=" + providerId + ", providerName=" + providerName + ", secretName=" + secretName + ", fullyQualifiedName=" + fullyQualifiedName + "]";
    }
}
