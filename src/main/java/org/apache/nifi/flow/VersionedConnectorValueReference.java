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

/**
 * Represents a property value reference for a Connector in a versioned flow.
 * This class is used for serialization/deserialization of connector property values
 * that may reference different types of values (literals, assets, secrets).
 */
public class VersionedConnectorValueReference {
    private String valueType;
    private String value;
    private String assetId;
    private String providerId;
    private String providerName;
    private String secretGroupName;
    private String secretName;

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

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(final String assetId) {
        this.assetId = assetId;
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

    public String getSecretGroupName() {
        return secretGroupName;
    }

    public void setSecretGroupName(final String secretGroupName) {
        this.secretGroupName = secretGroupName;
    }

    public String getSecretName() {
        return secretName;
    }

    public void setSecretName(final String secretName) {
        this.secretName = secretName;
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
               && Objects.equals(assetId, other.assetId)
               && Objects.equals(providerId, other.providerId)
               && Objects.equals(secretGroupName, other.secretGroupName)
               && Objects.equals(secretName, other.secretName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueType, value, assetId, providerId, secretGroupName, secretName);
    }

    @Override
    public String toString() {
        return "VersionedConnectorValueReference[valueType=" + valueType + ", value=" + value
               + ", assetId=" + assetId + ", providerId=" + providerId + ", secretGroupName=" + secretGroupName + ", secretName=" + secretName + "]";
    }
}
