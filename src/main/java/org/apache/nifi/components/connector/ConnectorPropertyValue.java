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

import org.apache.nifi.processor.DataUnit;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface ConnectorPropertyValue {

    /**
     * @return the raw property value as a string
     */
    String getValue();

    /**
     * @return an integer representation of the property value, or
     * <code>null</code> if not set
     * @throws NumberFormatException if not able to parse
     */
    Integer asInteger();

    /**
     * @return a Long representation of the property value, or <code>null</code>
     * if not set
     * @throws NumberFormatException if not able to parse
     */
    Long asLong();

    /**
     * @return a Boolean representation of the property value, or
     * <code>null</code> if not set
     */
    Boolean asBoolean();

    /**
     * @return a Float representation of the property value, or
     * <code>null</code> if not set
     * @throws NumberFormatException if not able to parse
     */
    Float asFloat();

    /**
     * @return a Double representation of the property value, of
     * <code>null</code> if not set
     * @throws NumberFormatException if not able to parse
     */
    Double asDouble();

    /**
     * @param timeUnit specifies the TimeUnit to convert the time duration into
     * @return a Long value representing the value of the configured time period
     * in terms of the specified TimeUnit; if the property is not set, returns
     * <code>null</code>
     */
    Long asTimePeriod(TimeUnit timeUnit);

    /**
     * Returns the value as a Duration
     *
     * @return a Duration representing the value, or <code>null</code> if the value is unset
     */
    Duration asDuration();

    /**
     *
     * @param dataUnit specifies the DataUnit to convert the data size into
     * @return a Long value representing the value of the configured data size
     * in terms of the specified DataUnit; if hte property is not set, returns
     * <code>null</code>
     */
    Double asDataSize(DataUnit dataUnit);

    /**
     * Returns the value as a comma-separated list of values. Leading and trailing
     * whitespace is trimmed from each value.
     * @return the property value as a List of Strings
     */
    List<String> asList();

    /**
     * @return <code>true</code> if the user has configured a value, or if the
     * {@link ConnectorPropertyDescriptor} for the associated property has a default
     * value, <code>false</code> otherwise
     */
    boolean isSet();

}
