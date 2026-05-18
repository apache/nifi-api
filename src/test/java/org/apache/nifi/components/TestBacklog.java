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
package org.apache.nifi.components;

import org.apache.nifi.components.Backlog.Precision;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBacklog {

    @Test
    public void testPlusSumsDimensionsAndKeepsExactWhenBothSidesReportTheSameShape() {
        final Backlog left = Backlog.builder().flowFiles(10L).bytes(100L).build();
        final Backlog right = Backlog.builder().flowFiles(5L).bytes(50L).build();

        final Backlog combined = left.plus(right);

        assertEquals(OptionalLong.of(15L), combined.getFlowFileCount());
        assertEquals(OptionalLong.of(150L), combined.getByteCount());
        assertFalse(combined.getRecordCount().isPresent());
        assertEquals(Precision.EXACT, combined.getPrecision());
    }

    @Test
    public void testPlusDowngradesToAtLeastWhenOneSideOmitsADimensionTheOtherReports() {
        // Left knows only flowFiles; right knows only bytes. The result should still carry the known
        // numeric values, but the precision must be AT_LEAST because each side is "unknown" with
        // respect to the dimension the other side reported. Treating "unknown" as zero would let the
        // combined Backlog falsely claim EXACT completeness.
        final Backlog left = Backlog.flowFiles(10L);
        final Backlog right = Backlog.bytes(100L);

        final Backlog combined = left.plus(right);

        assertEquals(OptionalLong.of(10L), combined.getFlowFileCount());
        assertEquals(OptionalLong.of(100L), combined.getByteCount());
        assertFalse(combined.getRecordCount().isPresent());
        assertEquals(Precision.AT_LEAST, combined.getPrecision());
    }

    @Test
    public void testPlusDowngradesToAtLeastWhenAsymmetricEvenIfBothSidesAreExact() {
        // Both sides are EXACT individually but report different sets of dimensions. The combined
        // view cannot be EXACT because each side is silent about a dimension the other side knows.
        final Backlog left = Backlog.builder().flowFiles(1L).bytes(2L).precision(Precision.EXACT).build();
        final Backlog right = Backlog.builder().flowFiles(3L).bytes(4L).records(5L).precision(Precision.EXACT).build();

        final Backlog combined = left.plus(right);

        assertEquals(OptionalLong.of(4L), combined.getFlowFileCount());
        assertEquals(OptionalLong.of(6L), combined.getByteCount());
        assertEquals(OptionalLong.of(5L), combined.getRecordCount());
        assertEquals(Precision.AT_LEAST, combined.getPrecision());
    }

    @Test
    public void testPlusPropagatesAtLeastFromEitherOperand() {
        final Backlog left = Backlog.builder().flowFiles(1L).precision(Precision.AT_LEAST).build();
        final Backlog right = Backlog.builder().flowFiles(2L).precision(Precision.EXACT).build();

        assertEquals(Precision.AT_LEAST, left.plus(right).getPrecision());
        assertEquals(Precision.AT_LEAST, right.plus(left).getPrecision());
    }

    @Test
    public void testPlusUsesEarlierLastCaughtUpAndKeepsOnlySideWhenOtherMissing() {
        final Instant earlier = Instant.parse("2025-01-01T00:00:00Z");
        final Instant later = Instant.parse("2025-01-02T00:00:00Z");

        final Backlog withEarlier = Backlog.builder().flowFiles(0L).lastCaughtUp(earlier).build();
        final Backlog withLater = Backlog.builder().flowFiles(0L).lastCaughtUp(later).build();
        assertEquals(earlier, withEarlier.plus(withLater).getLastCaughtUp().orElseThrow());

        final Backlog withoutTimestamp = Backlog.flowFiles(0L);
        assertEquals(later, withoutTimestamp.plus(withLater).getLastCaughtUp().orElseThrow());
    }

    @Test
    public void testPlusOverflowOnLongSum() {
        final Backlog left = Backlog.flowFiles(Long.MAX_VALUE);
        final Backlog right = Backlog.flowFiles(1L);
        assertThrows(ArithmeticException.class, () -> left.plus(right));
    }

    @Test
    public void testCaughtUpFactoryProducesZerosAndTimestamp() {
        final Backlog caughtUp = Backlog.caughtUp();
        assertEquals(OptionalLong.of(0L), caughtUp.getFlowFileCount());
        assertEquals(OptionalLong.of(0L), caughtUp.getByteCount());
        assertEquals(OptionalLong.of(0L), caughtUp.getRecordCount());
        assertTrue(caughtUp.getLastCaughtUp().isPresent());
        assertEquals(Precision.EXACT, caughtUp.getPrecision());
    }
}
