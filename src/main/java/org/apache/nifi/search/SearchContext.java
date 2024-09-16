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
package org.apache.nifi.search;

import java.util.Map;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;

public interface SearchContext {

    /**
     * @return the search term
     */
    String getSearchTerm();

    /**
     * @return the annotation data
     */
    String getAnnotationData();

    /**
     * @param property to get value of
     * @return a PropertyValue that encapsulates the value configured for the
     * given PropertyDescriptor
     */
    PropertyValue getProperty(PropertyDescriptor property);

    /**
     * @return a Map of all configured Properties
     */
    Map<PropertyDescriptor, String> getProperties();
}
