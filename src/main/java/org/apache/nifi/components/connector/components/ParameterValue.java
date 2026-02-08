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

package org.apache.nifi.components.connector.components;

import org.apache.nifi.asset.Asset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ParameterValue {
    private final String name;
    private final String value;
    private final boolean sensitive;
    private final List<Asset> assets;

    private ParameterValue(final Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.sensitive = builder.sensitive;
        this.assets = Collections.unmodifiableList(builder.referencedAssets);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    @Override
    public String toString() {
        return "ParameterValue[name=" + name + ", value=" + (sensitive ? "****" : value) + ", sensitive=" + sensitive + ", assets=" + assets + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ParameterValue that = (ParameterValue) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public static class Builder {
        private String name;
        private String value;
        private boolean sensitive;
        private final List<Asset> referencedAssets = new ArrayList<>();

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder value(final String value) {
            this.value = value;
            return this;
        }

        public Builder sensitive(final boolean sensitive) {
            this.sensitive = sensitive;
            return this;
        }

        public Builder addReferencedAsset(final Asset asset) {
            this.referencedAssets.add(asset);
            return this;
        }

        public ParameterValue build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Parameter name must be provided");
            }
            if (value != null && !referencedAssets.isEmpty()) {
                throw new IllegalStateException("Parameter cannot have both a value and referenced assets");
            }
            return new ParameterValue(this);
        }
    }
}
