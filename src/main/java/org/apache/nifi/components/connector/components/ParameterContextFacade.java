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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

public interface ParameterContextFacade {

    /**
     * Updates the parameters in the Parameter Context with the given collection of ParameterValue objects.
     * If a parameter does not already exist, it will be created. If it does exist, its value will be updated.
     * If any parameter already exists but is not included in the given collection, it will remain unchanged.
     *
     * @param parameterValues the collection of ParameterValue objects to set or update in the Parameter Context
     * @throws IllegalArgumentException if the sensitivity of a parameter does not match the existing parameter's sensitivity
     */
    void updateParameters(Collection<ParameterValue> parameterValues);

    /**
     * Gets the value of a parameter from the Parameter Context.
     * @param parameterName the name of the parameter to retrieve
     * @return the value of the parameter, or null if it is not set
     */
    String getValue(String parameterName);

    /**
     * Returns the names of all parameters that have been set in the Parameter Context.
     * @return the names of all parameters that have been set in the Parameter Context.
     */
    Set<String> getDefinedParameterNames();

    /**
     * Checks if a parameter is marked as sensitive in the Parameter Context.
     * @param parameterName the name of the parameter to check
     * @return true if the parameter is marked as sensitive, false if it is not sensitive or is not known
     */
    boolean isSensitive(String parameterName);

    /**
     * Creates an asset whose contents are provided by the given InputStream. The asset may then be associated with a parameter
     * by creating a ParameterValue that references the asset and updating parameters via updateParameters(Collection).
     *
     * @param inputStream the InputStream containing the asset contents
     * @throws IOException if an error occurs while reading from the InputStream or storing the asset
     * @return the asset that was created
     */
    Asset createAsset(InputStream inputStream) throws IOException;
}
