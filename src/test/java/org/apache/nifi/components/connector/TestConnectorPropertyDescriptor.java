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

import org.apache.nifi.components.DescribedValue;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestConnectorPropertyDescriptor {

    private static final String TEST_STEP_NAME = "test-step";
    private static final String TEST_GROUP_NAME = "test-group";

    @Test
    void testRequiredProperty() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Required Property")
            .type(PropertyType.STRING)
            .required(true)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, null, context).isValid());
    }

    @Test
    void testValidateStringType() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("String Property")
            .type(PropertyType.STRING)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "any string value", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "!@#$%^&*()_+-=[]{}|;:',.<>?/~`", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "", context).isValid());
    }

    @Test
    void testValidatePasswordType() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Password Property")
            .type(PropertyType.SECRET)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "secretPassword123!", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "", context).isValid());
    }

    @Test
    void testValidateStringListType() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("String List Property")
            .type(PropertyType.STRING_LIST)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "item1,item2,item3", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "", context).isValid());
    }

    @Test
    void testValidateBooleanTypeWithValidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Boolean Property")
            .type(PropertyType.BOOLEAN)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "true", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "false", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "TRUE", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "FALSE", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "TrUe", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "FaLsE", context).isValid());
    }

    @Test
    void testValidateBooleanTypeWithInvalidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Boolean Property")
            .type(PropertyType.BOOLEAN)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        ValidationResult result = descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "invalid", context);
        assertFalse(result.isValid());
        assertEquals("Boolean Property", result.getSubject());
        assertEquals("invalid", result.getInput());
        assertEquals("Value must be true or false", result.getExplanation());

        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "1", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "0", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "yes", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "no", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "", context).isValid());
    }

    @Test
    void testValidateIntegerTypeWithValidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Integer Property")
            .type(PropertyType.INTEGER)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "12345", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "-12345", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "0", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "00123", context).isValid());
    }

    @Test
    void testValidateIntegerTypeWithInvalidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Integer Property")
            .type(PropertyType.INTEGER)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        ValidationResult result = descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123.45", context);
        assertFalse(result.isValid());
        assertEquals("Integer Property", result.getSubject());
        assertEquals("123.45", result.getInput());
        assertEquals("Value must be an integer", result.getExplanation());

        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "not a number", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, " 123 ", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "+123", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "0x1A3F", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "", context).isValid());
    }

    @Test
    void testValidateDoubleTypeWithValidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Double Property")
            .type(PropertyType.DOUBLE)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123.456", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "-123.456", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "0.0", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "0", context).isValid());
    }

    @Test
    void testValidateDoubleTypeWithInvalidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Double Property")
            .type(PropertyType.DOUBLE)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        ValidationResult result = descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "not a number", context);
        assertFalse(result.isValid());
        assertEquals("Double Property", result.getSubject());
        assertEquals("not a number", result.getInput());
        assertEquals("Value must be a floating point number", result.getExplanation());

        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123.456.789", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "1.23e10", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123.", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, ".123", context).isValid());
    }

    @Test
    void testValidateFloatTypeWithValidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Float Property")
            .type(PropertyType.FLOAT)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123.456", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "-123.456", context).isValid());
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "0", context).isValid());
    }

    @Test
    void testValidateFloatTypeWithInvalidValues() {
        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Float Property")
            .type(PropertyType.FLOAT)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        ValidationResult result = descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "not a number", context);
        assertFalse(result.isValid());
        assertEquals("Float Property", result.getSubject());
        assertEquals("not a number", result.getInput());
        assertEquals("Value must be a floating point number", result.getExplanation());

        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, "123.", context).isValid());
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, ".123", context).isValid());
    }

    @Test
    void testValidateAssetTypeWithExistingAsset(@TempDir final Path tempDir) throws IOException {
        final File assetFile = createAssetFile(tempDir, "asset1.txt");

        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Asset Property")
            .type(PropertyType.ASSET)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, assetFile.getAbsolutePath(), context).isValid());
    }

    @Test
    void testValidateAssetTypeWithMissingAsset(@TempDir final Path tempDir) throws IOException {
        final File assetFile = new File(tempDir.toFile(), "asset1.txt");

        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Asset Property")
            .type(PropertyType.ASSET)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, assetFile.getAbsolutePath(), context).isValid());
    }

    @Test
    void testValidateAssetTypeWithMultipleAssets(@TempDir final Path tempDir) throws IOException {
        final File assetFile1 = new File(tempDir.toFile(), "asset1.txt");
        final File assetFile2 = new File(tempDir.toFile(), "asset2.txt");
        final String multipleAssetValue = assetFile1.getAbsolutePath() + "," + assetFile2.getAbsolutePath();

        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Asset Property")
            .type(PropertyType.ASSET)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, multipleAssetValue, context).isValid());
    }

    @Test
    void testValidateAssetListTypeWithSingleExistingAsset(@TempDir final Path tempDir) throws IOException {
        final File assetFile1 = createAssetFile(tempDir, "asset1.txt");

        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Asset List Property")
            .type(PropertyType.ASSET_LIST)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, assetFile1.getAbsolutePath(), context).isValid());
    }

    @Test
    void testValidateAssetListTypeWithMultipleExistingAssets(@TempDir final Path tempDir) throws IOException {
        final File assetFile1 = createAssetFile(tempDir, "asset1.txt");
        final File assetFile2 = createAssetFile(tempDir, "asset2.txt");
        final String multipleAssetValue = assetFile1.getAbsolutePath() + "," + assetFile2.getAbsolutePath();

        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Asset List Property")
            .type(PropertyType.ASSET_LIST)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertTrue(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, multipleAssetValue, context).isValid());
    }

    @Test
    void testValidateAssetListTypeWithSomeMissingAssets(@TempDir final Path tempDir) throws IOException {
        final File assetFile1 = createAssetFile(tempDir, "asset1.txt");
        final File assetFile2 = new File(tempDir.toFile(), "asset2.txt"); // Never created
        final String multipleAssetValue = assetFile1.getAbsolutePath() + "," + assetFile2.getAbsolutePath();

        final ConnectorPropertyDescriptor descriptor = new ConnectorPropertyDescriptor.Builder()
            .name("Asset List Property")
            .type(PropertyType.ASSET_LIST)
            .build();

        final ConnectorValidationContext context = new TestConnectorValidationContext();
        assertFalse(descriptor.validate(TEST_STEP_NAME, TEST_GROUP_NAME, multipleAssetValue, context).isValid());
    }

    private File createAssetFile(final Path parentDir, final String name) throws IOException {
        final File assetFile = new File(parentDir.toFile(), name);
        try (final OutputStream outputStream = new FileOutputStream(assetFile)) {
            outputStream.write(name.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
        return assetFile;
    }

    /**
     * Simple test implementation of ConnectorValidationContext for unit testing.
     */
    private static class TestConnectorValidationContext implements ConnectorValidationContext {
        @Override
        public ValidationContext createValidationContext(final String stepName, final String groupName) {
            // Return null as it's not needed for basic type validation tests
            return null;
        }

        @Override
        public List<DescribedValue> fetchAllowableValues(final String stepName, final String propertyName) {
            // Return empty list as we don't need to fetch dynamic allowable values in these tests
            return Collections.emptyList();
        }
    }
}

