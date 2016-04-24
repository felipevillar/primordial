package com.villarsolutions.primordial.calculator.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a section of the number line for which
 * we wish to compute prime numbers.
 * <p>
 * The section starts on the number represented by the
 * <code>lowerBound</code> field and ends
 * on the number given by (<code>lowerBound</code> + <code>segmentSize</code>).
 * <p>
 * This class is immutable and therefore thread-safe.
 */
public class Segment {

    /**
     * The first number included in this segment. For example,
     * if this segment represents an inclusive range of numbers
     * from 6 to 9, the lowerBound is 6
     */
    private long lowerBound;

    /**
     * The number of numbers included in this segment.  For example,
     * if this segment represents the inclusive range from 6 to 9,
     * the segmentSize is 4.
     * <p>
     * Segments are equally sized apart from the last segment which
     * might be smaller than the other ones.
     */
    private int segmentSize;

    public Segment() {
        // JSON de-serialization
    }

    private Segment(long lowerBound, int segmentSize) {
        this.lowerBound = lowerBound;
        this.segmentSize = segmentSize;
    }

    public static Segment create(long lowerBound, int segmentSize) {
        return new Segment(lowerBound, segmentSize);
    }

    public long getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(long lowerBound) {
        this.lowerBound = lowerBound;
    }

    public int getSegmentSize() {
        return segmentSize;
    }

    public long getUpperBound() {
        return lowerBound + segmentSize - 1;
    }

    public void setSegmentSize(int segmentSize) {
        this.segmentSize = segmentSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Segment segment = (Segment) o;

        return new EqualsBuilder()
            .append(lowerBound, segment.lowerBound)
            .append(segmentSize, segment.segmentSize)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(lowerBound)
            .append(segmentSize)
            .toHashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
