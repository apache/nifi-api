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
 * A ConnectorValueReference implementation representing a string literal value.
 */
public final class StringLiteralValue implements ConnectorValueReference {

    /**
     * An empty value reference, represented as a StringLiteralValue with a null value.
     */
    public static final StringLiteralValue EMPTY = new StringLiteralValue(null);

    private final String value;

    public StringLiteralValue(final String value) {
        this.value = value;
    }

    /**
     * Returns the string literal value.
     *
     * @return the string literal value
     */
    public String getValue() {
        return value;
    }

    @Override
    public ConnectorValueType getValueType() {
        return ConnectorValueType.STRING_LITERAL;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final StringLiteralValue that = (StringLiteralValue) object;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "StringLiteralValue[value=" + value + "]";
    }
}
