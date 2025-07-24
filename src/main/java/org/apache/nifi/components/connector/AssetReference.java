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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A ConnectorValueReference implementation representing a reference to one or more assets.
 */
public final class AssetReference implements ConnectorValueReference {

    private final Set<String> assetIdentifiers;

    public AssetReference(final Set<String> assetIdentifiers) {
        this.assetIdentifiers = assetIdentifiers == null ? Collections.emptySet() : new HashSet<>(assetIdentifiers);
    }

    /**
     * Returns the asset identifiers.
     *
     * @return the asset identifiers
     */
    public Set<String> getAssetIdentifiers() {
        return assetIdentifiers;
    }

    @Override
    public ConnectorValueType getValueType() {
        return ConnectorValueType.ASSET_REFERENCE;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final AssetReference that = (AssetReference) object;
        return Objects.equals(assetIdentifiers, that.assetIdentifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(assetIdentifiers);
    }

    @Override
    public String toString() {
        return "AssetReference[assetIds=" + assetIdentifiers + "]";
    }
}
