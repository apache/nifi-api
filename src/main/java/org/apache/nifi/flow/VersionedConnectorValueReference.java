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

package org.apache.nifi.flow;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a property value reference for a Connector in a versioned flow.
 * This class is used for serialization/deserialization of connector property values
 * that may reference different types of values (literals, assets, secrets).
 */
public class VersionedConnectorValueReference {
    private String valueType;
    private String value;
    private String providerId;
    private String providerName;
    private String secretName;
    private String fullyQualifiedSecretName;
    private Set<String> assetIds;

    public String getValueType() {
        return valueType;
    }

    public void setValueType(final String valueType) {
        this.valueType = valueType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(final String secretName) {
        this.secretName = secretName;
    }

    public String getFullyQualifiedSecretName() {
        return fullyQualifiedSecretName;
    }

    public void setFullyQualifiedSecretName(final String fullyQualifiedSecretName) {
        this.fullyQualifiedSecretName = fullyQualifiedSecretName;
    }

    public Set<String> getAssetIds() {
        return assetIds;
    }

    public void setAssetIds(final Set<String> assetIds) {
        this.assetIds = assetIds;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VersionedConnectorValueReference other)) {
            return false;
        }
        return Objects.equals(valueType, other.valueType)
               && Objects.equals(value, other.value)
               && Objects.equals(assetIds, other.assetIds)
               && Objects.equals(providerId, other.providerId)
               && Objects.equals(secretName, other.secretName)
               && Objects.equals(fullyQualifiedSecretName, other.fullyQualifiedSecretName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueType, value, assetIds, providerId, secretName, fullyQualifiedSecretName);
    }

    @Override
    public String toString() {
        return "VersionedConnectorValueReference[valueType=" + valueType + ", value=" + value
               + ", assetIds=" + assetIds + ", providerId=" + providerId + ", fullyQualifiedSecretName=" + fullyQualifiedSecretName + "]";
    }
}
