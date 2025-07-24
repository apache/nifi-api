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

import java.util.Map;
import java.util.Objects;

public class StepConfiguration {
    private final Map<String, ConnectorValueReference> propertyValues;

    public StepConfiguration(final Map<String, ConnectorValueReference> propertyValues) {
        this.propertyValues = propertyValues;
    }

    public Map<String, ConnectorValueReference> getPropertyValues() {
        return propertyValues;
    }

    public ConnectorValueReference getPropertyValue(final String propertyName) {
        return propertyValues.get(propertyName);
    }

    @Override
    public String toString() {
        return "StepConfiguration[" +
               "propertyValues=" + propertyValues +
               "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StepConfiguration that = (StepConfiguration) o;
        return Objects.equals(propertyValues, that.propertyValues);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(propertyValues);
    }
}
