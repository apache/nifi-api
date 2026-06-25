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

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * <p>
 *     An immutable description of how much work remains for a Connector or Processor
 *     to consume from its source system. A {@code Backlog} may report any combination of
 *     FlowFile count, byte count, and record count, and may also indicate the most recent
 *     time at which the component observed itself as fully caught up with the source.
 * </p>
 *
 * <p>
 *     All four dimensions are optional and independent. A component reports only those
 *     dimensions that it can determine. For example, a Connector pulling from an object
 *     store may know both the number of objects remaining and the total bytes those
 *     objects represent, while a Connector pulling from a streaming system may know only
 *     the number of records remaining.
 * </p>
 *
 * <p>
 *     The numeric dimensions are interpreted in light of the {@link Precision} attribute,
 *     which applies to the entire {@code Backlog}. {@link Precision#EXACT} (the default)
 *     means the values are exact counts; {@link Precision#AT_LEAST} means the values are
 *     lower bounds and the real counts may be higher.
 * </p>
 *
 * <p>
 *     The {@link #getLastCaughtUp() lastCaughtUp} timestamp is unaffected by precision and
 *     is always treated as exact. It represents the most recent moment the component
 *     determined that there was zero data remaining to pull from the source. It is computed
 *     by the component using whatever knowledge it has — typically during its normal polling
 *     of the source — and is not merely the time a backlog query last returned zero.
 * </p>
 *
 * <p>
 *     Backlogs are composed via {@link #plus(Backlog)}; see that method for the per-field
 *     combination rules.
 * </p>
 *
 * <p>
 *     This API is experimental and may change without notice between releases.
 * </p>
 */
public final class Backlog {

    private final OptionalLong flowFileCount;
    private final OptionalLong byteCount;
    private final OptionalLong recordCount;
    private final Optional<Instant> lastCaughtUp;
    private final Precision precision;

    private Backlog(final Builder builder) {
        this.flowFileCount = builder.flowFileCount;
        this.byteCount = builder.byteCount;
        this.recordCount = builder.recordCount;
        this.lastCaughtUp = builder.lastCaughtUp;
        this.precision = builder.precision;
    }

    /**
     * @return the number of FlowFiles remaining on the source, if known
     */
    public OptionalLong getFlowFileCount() {
        return flowFileCount;
    }

    /**
     * @return the total number of bytes remaining on the source, if known
     */
    public OptionalLong getByteCount() {
        return byteCount;
    }

    /**
     * @return the number of records remaining on the source, if known
     */
    public OptionalLong getRecordCount() {
        return recordCount;
    }

    /**
     * @return the most recent moment the component observed itself as fully caught up with the source, if known
     */
    public Optional<Instant> getLastCaughtUp() {
        return lastCaughtUp;
    }

    /**
     * @return the {@link Precision} of the numeric dimensions of this Backlog. Never null.
     */
    public Precision getPrecision() {
        return precision;
    }

    /**
     * <p>
     *     Combines this Backlog with another to produce a new {@code Backlog} describing
     *     the union of the two. Useful for composing a flow-wide backlog from multiple sources
     *     such as multiple Processor reports and queue snapshots.
     * </p>
     *
     * <p>
     *     Combination rules:
     * </p>
     * <ul>
     *     <li>
     *         Numeric dimensions ({@code flowFileCount}, {@code byteCount}, {@code recordCount}):
     *         if both sides have a value, the result is their sum. If only one side has a value,
     *         it is carried through but the combined {@link Precision} is downgraded to
     *         {@link Precision#AT_LEAST} (see the precision rule below) when the other side reports
     *         at least one numeric dimension, because the side that did not report the dimension is
     *         unknown rather than zero. If the other side reports no numeric dimensions at all, the
     *         known value is carried through without adding numeric uncertainty. If neither side has
     *         a value, the field stays empty.
     *     </li>
     *     <li>
     *         {@link #getLastCaughtUp() lastCaughtUp}: <b>not summable.</b> If both sides have a value,
     *         the result is the earlier of the two timestamps — the more conservative claim about how
     *         recently the system was fully caught up. If only one side has it, that one is carried
     *         through.
     *     </li>
     *     <li>
     *         {@link #getPrecision() precision}: the result is {@link Precision#EXACT} only when both
     *         sides are {@code EXACT} <i>and</i> either both sides report the same set of populated
     *         numeric dimensions or one side reports no numeric dimensions at all. Otherwise the
     *         result is {@link Precision#AT_LEAST}. Any uncertainty in either operand — including a
     *         missing dimension on one side that the other side reported — taints the result,
     *         because "unknown" must not be treated as zero.
     *     </li>
     * </ul>
     *
     * @param other the other Backlog to combine with this one
     * @return a new Backlog representing the combination
     * @throws ArithmeticException if a numeric sum would overflow {@code long}
     */
    public Backlog plus(final Backlog other) {
        Objects.requireNonNull(other, "other Backlog must not be null");

        final OptionalLong sumFlowFiles = sumOptional(flowFileCount, other.flowFileCount);
        final OptionalLong sumBytes = sumOptional(byteCount, other.byteCount);
        final OptionalLong sumRecords = sumOptional(recordCount, other.recordCount);
        final Optional<Instant> earliestCaughtUp = earlierOf(lastCaughtUp, other.lastCaughtUp);

        final boolean reportsNumericDimensions = flowFileCount.isPresent() || byteCount.isPresent() || recordCount.isPresent();
        final boolean otherReportsNumericDimensions = other.flowFileCount.isPresent() || other.byteCount.isPresent() || other.recordCount.isPresent();

        // A dimension that one side reports and the other does not is "unknown" on the omitting side,
        // not zero. Carrying the known value forward and still reporting EXACT would let the result
        // claim completeness it cannot back up, so any such asymmetry forces AT_LEAST. However, a
        // Backlog with no numeric dimensions contributes only non-numeric information, such as a
        // lastCaughtUp timestamp, and therefore does not make numeric counts less exact.
        final boolean dimensionsAsymmetric = reportsNumericDimensions && otherReportsNumericDimensions
                && (flowFileCount.isPresent() != other.flowFileCount.isPresent()
                || byteCount.isPresent() != other.byteCount.isPresent()
                || recordCount.isPresent() != other.recordCount.isPresent());
        final boolean precisionExact = precision == Precision.EXACT || !reportsNumericDimensions;
        final boolean otherPrecisionExact = other.precision == Precision.EXACT || !otherReportsNumericDimensions;
        final Precision combinedPrecision = (precisionExact && otherPrecisionExact && !dimensionsAsymmetric) ? Precision.EXACT : Precision.AT_LEAST;

        final Builder builder = new Builder().precision(combinedPrecision);
        if (sumFlowFiles.isPresent()) {
            builder.flowFiles(sumFlowFiles.getAsLong());
        }

        if (sumBytes.isPresent()) {
            builder.bytes(sumBytes.getAsLong());
        }

        if (sumRecords.isPresent()) {
            builder.records(sumRecords.getAsLong());
        }

        earliestCaughtUp.ifPresent(builder::lastCaughtUp);
        return builder.build();
    }

    private static OptionalLong sumOptional(final OptionalLong left, final OptionalLong right) {
        if (left.isPresent() && right.isPresent()) {
            return OptionalLong.of(Math.addExact(left.getAsLong(), right.getAsLong()));
        }

        if (left.isPresent()) {
            return left;
        }

        if (right.isPresent()) {
            return right;
        }

        return OptionalLong.empty();
    }

    private static Optional<Instant> earlierOf(final Optional<Instant> left, final Optional<Instant> right) {
        if (left.isPresent() && right.isPresent()) {
            return left.get().isBefore(right.get()) ? left : right;
        }

        if (left.isPresent()) {
            return left;
        }

        return right;
    }

    /**
     * Creates a Backlog whose only populated dimension is the FlowFile count, with {@link Precision#EXACT}.
     *
     * @param count the number of FlowFiles remaining on the source; must be non-negative
     * @return a new Backlog
     */
    public static Backlog flowFiles(final long count) {
        return new Builder().flowFiles(count).build();
    }

    /**
     * Creates a Backlog whose only populated dimension is the byte count, with {@link Precision#EXACT}.
     *
     * @param bytes the number of bytes remaining on the source; must be non-negative
     * @return a new Backlog
     */
    public static Backlog bytes(final long bytes) {
        return new Builder().bytes(bytes).build();
    }

    /**
     * Creates a Backlog whose only populated dimension is the record count, with {@link Precision#EXACT}.
     *
     * @param count the number of records remaining on the source; must be non-negative
     * @return a new Backlog
     */
    public static Backlog records(final long count) {
        return new Builder().records(count).build();
    }

    /**
     * Creates a Backlog populated with the provided {@code lastCaughtUp} timestamp. Useful for
     * combining with a count-only Backlog via {@link #plus(Backlog)}.
     *
     * @param instant the moment at which the component was last observed as fully caught up; may be
     *        null, in which case the returned Backlog has no populated dimensions
     * @return a new Backlog
     */
    public static Backlog lastCaughtUp(final Instant instant) {
        return new Builder().lastCaughtUp(instant).build();
    }

    /**
     * <p>
     *     Returns the canonical "fully caught up" Backlog: zero FlowFiles, zero bytes, zero records,
     *     and a {@code lastCaughtUp} timestamp of {@link Instant#now()}.
     * </p>
     *
     * <p>
     *     This state is the unambiguous "I am fully synchronized with the source as of now" signal.
     * </p>
     *
     * @return a new "caught up" Backlog
     */
    public static Backlog caughtUp() {
        return new Builder()
                .flowFiles(0L)
                .bytes(0L)
                .records(0L)
                .lastCaughtUp(Instant.now())
                .build();
    }

    /**
     * @return a new {@link Builder} for constructing a Backlog with multiple dimensions and/or {@link Precision#AT_LEAST}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Backlog)) {
            return false;
        }

        final Backlog other = (Backlog) object;
        return Objects.equals(flowFileCount, other.flowFileCount)
                && Objects.equals(byteCount, other.byteCount)
                && Objects.equals(recordCount, other.recordCount)
                && Objects.equals(lastCaughtUp, other.lastCaughtUp)
                && precision == other.precision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowFileCount, byteCount, recordCount, lastCaughtUp, precision);
    }

    @Override
    public String toString() {
        return "Backlog["
                + "flowFileCount=" + flowFileCount
                + ", byteCount=" + byteCount
                + ", recordCount=" + recordCount
                + ", lastCaughtUp=" + lastCaughtUp
                + ", precision=" + precision
                + "]";
    }

    /**
     * Builder for constructing a {@link Backlog} that populates more than one dimension or uses a
     * non-default {@link Precision}. For single-dimension Backlogs, prefer the static factory methods
     * such as {@link Backlog#flowFiles(long)}.
     */
    public static final class Builder {

        private OptionalLong flowFileCount = OptionalLong.empty();
        private OptionalLong byteCount = OptionalLong.empty();
        private OptionalLong recordCount = OptionalLong.empty();
        private Optional<Instant> lastCaughtUp = Optional.empty();
        private Precision precision = Precision.EXACT;

        /**
         * Sets the FlowFile count. Overwrites any previously set value.
         *
         * @param count the number of FlowFiles remaining on the source; must be non-negative
         * @return this Builder
         * @throws IllegalArgumentException if {@code count} is negative
         */
        public Builder flowFiles(final long count) {
            requireNonNegative("flowFiles", count);
            this.flowFileCount = OptionalLong.of(count);
            return this;
        }

        /**
         * Sets the byte count. Overwrites any previously set value.
         *
         * @param bytes the number of bytes remaining on the source; must be non-negative
         * @return this Builder
         * @throws IllegalArgumentException if {@code bytes} is negative
         */
        public Builder bytes(final long bytes) {
            requireNonNegative("bytes", bytes);
            this.byteCount = OptionalLong.of(bytes);
            return this;
        }

        /**
         * Sets the record count. Overwrites any previously set value.
         *
         * @param count the number of records remaining on the source; must be non-negative
         * @return this Builder
         * @throws IllegalArgumentException if {@code count} is negative
         */
        public Builder records(final long count) {
            requireNonNegative("records", count);
            this.recordCount = OptionalLong.of(count);
            return this;
        }

        /**
         * Sets the timestamp at which the component was last observed as fully caught up. Overwrites
         * any previously set value.
         *
         * @param instant the moment at which the component was last fully caught up; may be null to clear
         * @return this Builder
         */
        public Builder lastCaughtUp(final Instant instant) {
            this.lastCaughtUp = Optional.ofNullable(instant);
            return this;
        }

        /**
         * Sets the {@link Precision} for this Backlog. Defaults to {@link Precision#EXACT} if not set.
         *
         * @param precision the precision; must not be null
         * @return this Builder
         */
        public Builder precision(final Precision precision) {
            this.precision = Objects.requireNonNull(precision, "precision must not be null");
            return this;
        }

        /**
         * @return a new immutable {@link Backlog} reflecting the values set on this Builder
         */
        public Backlog build() {
            return new Backlog(this);
        }

        private static void requireNonNegative(final String fieldName, final long value) {
            if (value < 0L) {
                throw new IllegalArgumentException(fieldName + " must be non-negative but was " + value);
            }
        }
    }

    /**
     * <p>
     *     The precision of the numeric dimensions of a {@link Backlog}. Applies to the entire Backlog
     *     rather than to individual dimensions.
     * </p>
     */
    public enum Precision {

        /**
         * Numeric dimensions reflect exact counts.
         */
        EXACT,

        /**
         * Numeric dimensions are lower bounds; the real counts may be higher.
         */
        AT_LEAST
    }
}
