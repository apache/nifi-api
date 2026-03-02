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

import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.DescribedValue;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ConnectorPropertyDescriptor {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(?i)(true|false)$");

    private final String name;
    private final String description;
    private final String defaultValue;
    private final boolean required;
    private final PropertyType type;
    private final List<DescribedValue> allowableValues;
    private final boolean allowableValuesFetchable;
    private final List<Validator> validators;
    private final Set<ConnectorPropertyDependency> dependencies;

    private ConnectorPropertyDescriptor(final Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.defaultValue = builder.defaultValue;
        this.required = builder.required;
        this.type = builder.type;
        this.allowableValues = builder.allowableValues == null ? null : Collections.unmodifiableList(builder.allowableValues);
        this.allowableValuesFetchable = builder.allowableValuesFetchable;
        this.validators = List.copyOf(builder.validators);
        this.dependencies = Collections.unmodifiableSet(builder.dependencies);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public PropertyType getType() {
        return type;
    }

    public List<DescribedValue> getAllowableValues() {
        return allowableValues;
    }

    public boolean isAllowableValuesFetchable() {
        return allowableValuesFetchable;
    }

    public Set<ConnectorPropertyDependency> getDependencies() {
        return dependencies;
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public ValidationResult validate(final String stepName, final String groupName, final String value, final ConnectorValidationContext validationContext) {
        final List<DescribedValue> fetchedAllowableValues;
        if (isAllowableValuesFetchable()) {
            try {
                fetchedAllowableValues = validationContext.fetchAllowableValues(stepName, getName());
            } catch (final Exception e) {
                return new ValidationResult.Builder()
                    .subject(name)
                    .input(value)
                    .valid(false)
                    .explanation("Failed to fetch allowable values: " + e)
                    .build();
            }
        } else {
            fetchedAllowableValues = null;
        }

        if (required && value == null) {
            return new ValidationResult.Builder()
                .subject(name)
                .input(null)
                .valid(false)
                .explanation("Property is required but no value was specified")
                .build();
        }

        if (type == PropertyType.ASSET || type == PropertyType.ASSET_LIST) {
            return validateAssets(value);
        }

        if (type != PropertyType.STRING_LIST) {
            return validateIndividual(stepName, groupName, value, validationContext, fetchedAllowableValues);
        }

        final String[] values = value.split(",");
        for (final String individualValue : values) {
            final ValidationResult result = validateIndividual(stepName, groupName, individualValue.trim(), validationContext, fetchedAllowableValues);
            if (!result.isValid()) {
                return result;
            }
        }

        return new ValidationResult.Builder()
            .subject(name)
            .input(value)
            .valid(true)
            .build();
    }

    private ValidationResult validateIndividual(final String stepName, final String groupName, final String value,
                final ConnectorValidationContext validationContext, final List<DescribedValue> fetchedAllowableValues) {

        if (!isValueAllowed(value, allowableValues) || !isValueAllowed(value, fetchedAllowableValues)) {
            return new ValidationResult.Builder()
                .subject(name)
                .input(value)
                .valid(false)
                .explanation("Value is not one of the allowable values")
                .build();
        }

        final ValidationResult invalidResult = validateType(value);
        if (invalidResult != null) {
            return invalidResult;
        }

        for (final Validator validator : validators) {
            final ValidationResult result = validator.validate(name, value, validationContext.createValidationContext(stepName, groupName));
            if (!result.isValid()) {
                return result;
            }
        }

        return new ValidationResult.Builder()
            .subject(name)
            .input(value)
            .valid(true)
            .build();
    }

    private boolean isValueAllowed(final String value, final List<? extends DescribedValue> allowableValues) {
        if (allowableValues == null || allowableValues.isEmpty()) {
            // If no allowable values are explicitly specified, consider all values to be allowable
            return true;
        }
        if (value == null) {
            return false;
        }

        for (final DescribedValue describedValue : allowableValues) {
            if (value.equalsIgnoreCase(describedValue.getValue())) {
                return true;
            }
        }

        return false;
    }

    private ValidationResult validateType(final String value) {
        final String explanation = switch (type) {
            case SECRET, STRING, STRING_LIST, ASSET, ASSET_LIST -> null;
            case BOOLEAN -> BOOLEAN_PATTERN.matcher(value).matches() ? null : "Value must be true or false";
            case INTEGER -> INTEGER_PATTERN.matcher(value).matches() ? null : "Value must be an integer";
            case DOUBLE, FLOAT -> DOUBLE_PATTERN.matcher(value).matches() ? null : "Value must be a floating point number";
        };

        if (explanation == null) {
            return null;
        }

        return new ValidationResult.Builder()
            .subject(name)
            .input(value)
            .valid(false)
            .explanation(explanation)
            .build();
    }

    private ValidationResult validateAssets(final String value) {
        final String[] values = value.split(",");
        if (type == PropertyType.ASSET && values.length > 1) {
            return new ValidationResult.Builder()
                .subject(name)
                .input(value)
                .valid(false)
                .explanation("Property only supports a single asset, but " + values.length + " assets were specified")
                .build();
        }

        final List<String> nonExistentAssets = new ArrayList<>();
        for (final String assetValue : values) {
            final File assetFile = new File(assetValue);
            if (!assetFile.exists() || !assetFile.canRead()) {
                nonExistentAssets.add(assetValue);
            }
        }

        if (!nonExistentAssets.isEmpty()) {
            return new ValidationResult.Builder()
                .subject(name)
                .input(value)
                .valid(false)
                .explanation("The specified resource(s) do not exist or could not be accessed: " + nonExistentAssets)
                .build();
        }

        return new ValidationResult.Builder()
            .subject(name)
            .input(value)
            .valid(true)
            .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ConnectorPropertyDescriptor that = (ConnectorPropertyDescriptor) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "ConnectorPropertyDescriptor[name=" + name + "]";
    }

    public static final class Builder {
        private String name;
        private String description;
        private String defaultValue = null;
        private boolean required = false;
        private PropertyType type = PropertyType.STRING;
        private List<DescribedValue> allowableValues = null;
        private boolean allowableValuesFetchable = false;
        private final List<Validator> validators = new ArrayList<>();
        private final Set<ConnectorPropertyDependency> dependencies = new HashSet<>();

        public Builder from(final ConnectorPropertyDescriptor other) {
            this.name = other.name;
            this.description = other.description;
            this.defaultValue = other.defaultValue;
            this.required = other.required;
            this.type = other.type;
            this.allowableValues = other.allowableValues == null ? null : new ArrayList<>(other.allowableValues);
            this.allowableValuesFetchable = other.allowableValuesFetchable;
            this.validators.clear();
            this.validators.addAll(other.validators);
            this.dependencies.clear();
            this.dependencies.addAll(other.dependencies);
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(final String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder defaultValue(final DescribedValue defaultValue) {
            return defaultValue(defaultValue == null ? null : defaultValue.getValue());
        }

        public Builder required(final boolean required) {
            this.required = required;
            return this;
        }

        public Builder type(final PropertyType type) {
            this.type = type;
            return this;
        }

        public Builder allowableValuesFetchable(final boolean fetchable) {
            this.allowableValuesFetchable = fetchable;
            return this;
        }

        public Builder allowableValues(final DescribedValue... values) {
            this.allowableValues = Arrays.stream(values)
                .map(Builder::describedValue)
                .toList();

            return this;
        }

        public <E extends Enum<E>> Builder allowableValues(final E[] values) {
            if (values == null || values.length == 0) {
                this.allowableValues = null;
            } else {
                this.allowableValues = Arrays.stream(values)
                    .map(enumValue -> enumValue instanceof DescribedValue describedValue
                        ? AllowableValue.fromDescribedValue(describedValue) : new AllowableValue(enumValue.name()))
                    .map(av -> (DescribedValue) av)
                    .toList();
            }

            return this;
        }

        public <E extends Enum<E>> Builder allowableValues(final EnumSet<E> enumValues) {  //NOPMD
            if (enumValues == null || enumValues.isEmpty()) {
                this.allowableValues = null;
            } else {
                this.allowableValues = enumValues.stream()
                    .map(enumValue -> enumValue instanceof DescribedValue describedValue
                        ? AllowableValue.fromDescribedValue(describedValue) : new AllowableValue(enumValue.name()))
                    .map(av -> (DescribedValue) av)
                    .toList();
            }

            return this;
        }

        public Builder allowableValues(final String... allowableValues) {
            if (allowableValues == null || allowableValues.length == 0) {
                this.allowableValues = null;
            } else {
                this.allowableValues = Arrays.stream(allowableValues)
                    .map(Builder::describedValue)
                    .toList();
            }

            return this;
        }

        public Builder allowableValues(final List<String> allowableValues) {
            if (allowableValues == null || allowableValues.isEmpty()) {
                this.allowableValues = null;
            } else {
                this.allowableValues = allowableValues.stream()
                    .map(Builder::describedValue)
                    .toList();
            }

            return this;
        }

        /**
         * Adds a validator for this property
         *
         * @param validator the validator to add
         * @return this Builder for method chaining
         */
        public Builder addValidator(final Validator validator) {
            if (validator != null) {
                this.validators.add(validator);
            }
            return this;
        }

        /**
         * Removes all validators for this property
         *
         * @return this Builder for method chaining
         */
        public Builder clearValidators() {
            this.validators.clear();
            return this;
        }

        /**
         * Sets the validators for this property, replacing any previously added validators
         *
         * @param validators the validators to set
         * @return this Builder for method chaining
         */
        public Builder validators(final Validator... validators) {
            this.validators.clear();

            if (validators != null) {
                for (final Validator validator : validators) {
                    if (validator != null) {
                        this.validators.add(validator);
                    }
                }
            }

            return this;
        }

        public Builder dependsOn(final ConnectorPropertyDescriptor descriptor, final List<DescribedValue> dependentValues) {
            if (dependentValues == null || dependentValues.isEmpty()) {
                dependencies.add(new ConnectorPropertyDependency(descriptor.getName()));
            } else {
                final Set<String> dependentValueSet = dependentValues.stream()
                    .map(DescribedValue::getValue)
                    .collect(Collectors.toSet());

                dependencies.add(new ConnectorPropertyDependency(descriptor.getName(), dependentValueSet));
            }

            return this;
        }

        public Builder dependsOn(final ConnectorPropertyDescriptor descriptor, final DescribedValue firstDependentValue, final DescribedValue... additionalDependentValues) {
            final List<DescribedValue> dependentValues = new ArrayList<>();
            dependentValues.add(firstDependentValue);
            dependentValues.addAll(Arrays.asList(additionalDependentValues));
            return dependsOn(descriptor, dependentValues);
        }

        public Builder dependsOn(final ConnectorPropertyDescriptor descriptor, final String... dependentValues) {
            final List<DescribedValue> describedValues = Arrays.stream(dependentValues)
                    .map(Builder::describedValue)
                    .toList();

            return dependsOn(descriptor, describedValues);
        }

        private static DescribedValue describedValue(final String value) {
            if (value == null) {
                return null;
            }

            // Otherwise, return a generic DescribedValue with no display name or description
            return new AllowableValue(value);
        }

        private static DescribedValue describedValue(final DescribedValue describedValue) {
            if (describedValue == null) {
                return null;
            }

            return new AllowableValue(describedValue.getValue(), describedValue.getDisplayName(), describedValue.getDescription());
        }

        public ConnectorPropertyDescriptor build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Property name must be specified");
            }
            if (allowableValues != null && allowableValuesFetchable) {
                throw new IllegalStateException("Property cannot have both fetchable allowable values and a static list of allowable values");
            }

            return new ConnectorPropertyDescriptor(this);
        }
    }
}
