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

package org.apache.nifi.util;

import org.apache.nifi.processor.util.URLValidator;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestURLValidator {

    @Test
    void testValidUri() {
        final String url = "https://en.wikipedia.org/w/api.php?action=query&list=recentchanges&format=json&rcprop=user|comment|parsedcomment|timestamp|title|sizes|tags";
        assertDoesNotThrow(() -> URLValidator.createURL(url));
    }

    @Test
    void testInvalidUri() {
        final String uri = "http://   _";
        assertThrows(MalformedURLException.class, () -> URLValidator.createURL(uri));
    }
}
