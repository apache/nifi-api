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

package org.apache.nifi.migration;

import org.apache.nifi.components.connector.ConnectorPropertyDescriptor;
import org.apache.nifi.components.connector.ConnectorValueReference;

import java.util.Map;
import java.util.Optional;

/**
 * Step-scoped view of a {@link ConnectorPropertyConfiguration}. All operations apply to the step returned by
 * {@link #getStepName()}. Rename operations preserve the underlying {@link ConnectorValueReference} subtype (e.g. a
 * {@link org.apache.nifi.components.connector.SecretReference} stays a
 * {@link org.apache.nifi.components.connector.SecretReference}); use
 * {@link #setValueReference(String, ConnectorValueReference)} to change the subtype.
 *
 * <p>
 *     <b>Implementation Note:</b> This API is experimental and subject to change between minor releases.
 * </p>
 */
public interface ConnectorStepPropertyConfiguration {

    /**
     * @return the name of the configuration step this view is scoped to
     */
    String getStepName();

    /**
     * Renames an existing property, preserving its underlying {@link ConnectorValueReference}.
     *
     * @param propertyName the current property name
     * @param newName the new property name
     * @return <code>true</code> if the property was renamed; <code>false</code> if it did not exist
     */
    boolean renameProperty(String propertyName, String newName);

    /**
     * Removes the property with the given name, if it exists. No-op otherwise.
     *
     * @param propertyName the property name
     * @return <code>true</code> if the property was removed; <code>false</code> if it did not exist
     */
    boolean removeProperty(String propertyName);

    /**
     * @param propertyName the property name
     * @return <code>true</code> if this step has an entry for the property, including when its value reference is null
     */
    boolean hasProperty(String propertyName);

    /**
     * @param propertyName the property name
     * @return <code>true</code> if the property is set to a non-null value
     */
    boolean isPropertySet(String propertyName);

    /**
     * Convenience for {@code setValueReference(propertyName, new StringLiteralValue(propertyValue))}.
     */
    void setProperty(String propertyName, String propertyValue);

    /**
     * Sets the given property to the given {@link ConnectorValueReference}, or removes it when {@code valueReference}
     * is <code>null</code>.
     */
    void setValueReference(String propertyName, ConnectorValueReference valueReference);

    /**
     * Returns the string value when the underlying reference is a
     * {@link org.apache.nifi.components.connector.StringLiteralValue}. Empty when the property is absent, its
     * {@link org.apache.nifi.components.connector.StringLiteralValue} carries a null value, or the reference is a
     * different subtype (e.g. {@link org.apache.nifi.components.connector.AssetReference} or
     * {@link org.apache.nifi.components.connector.SecretReference}); use {@link #getValueReference(String)} for the
     * typed view.
     */
    Optional<String> getPropertyValue(String propertyName);

    /**
     * @return the underlying {@link ConnectorValueReference} regardless of subtype, or empty when the property is
     *          absent or the reference is <code>null</code>
     */
    Optional<ConnectorValueReference> getValueReference(String propertyName);

    /**
     * @return the string-literal properties in this step; properties whose values are of other subtypes are omitted
     *          (use {@link #getValueReferences()} for the full typed view)
     */
    Map<String, String> getProperties();

    /**
     * @return every property in this step keyed by name, exposing the underlying {@link ConnectorValueReference}
     */
    Map<String, ConnectorValueReference> getValueReferences();

    /**
     * Convenience overload of {@link #hasProperty(String)} accepting a {@link ConnectorPropertyDescriptor}.
     */
    default boolean hasProperty(final ConnectorPropertyDescriptor descriptor) {
        return hasProperty(descriptor.getName());
    }

    /**
     * Convenience overload of {@link #isPropertySet(String)} accepting a {@link ConnectorPropertyDescriptor}.
     */
    default boolean isPropertySet(final ConnectorPropertyDescriptor descriptor) {
        return isPropertySet(descriptor.getName());
    }

    /**
     * Convenience overload of {@link #setProperty(String, String)} accepting a {@link ConnectorPropertyDescriptor}.
     */
    default void setProperty(final ConnectorPropertyDescriptor descriptor, final String propertyValue) {
        setProperty(descriptor.getName(), propertyValue);
    }

    /**
     * Convenience overload of {@link #setValueReference(String, ConnectorValueReference)} accepting a
     * {@link ConnectorPropertyDescriptor}.
     */
    default void setValueReference(final ConnectorPropertyDescriptor descriptor, final ConnectorValueReference valueReference) {
        setValueReference(descriptor.getName(), valueReference);
    }

    /**
     * Convenience overload of {@link #getPropertyValue(String)} accepting a {@link ConnectorPropertyDescriptor}.
     */
    default Optional<String> getPropertyValue(final ConnectorPropertyDescriptor descriptor) {
        return getPropertyValue(descriptor.getName());
    }

    /**
     * Convenience overload of {@link #getValueReference(String)} accepting a {@link ConnectorPropertyDescriptor}.
     */
    default Optional<ConnectorValueReference> getValueReference(final ConnectorPropertyDescriptor descriptor) {
        return getValueReference(descriptor.getName());
    }
}
