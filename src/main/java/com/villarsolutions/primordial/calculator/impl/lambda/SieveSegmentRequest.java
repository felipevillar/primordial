package com.villarsolutions.primordial.calculator.impl.lambda;

import com.villarsolutions.primordial.calculator.impl.Segment;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Request POJO to contain the parameters needed for the SegmentedSieveLambda.calculatePrimesInSegment
 * function.
 * <p>
 * This class is serialized by the AWSLambdaClient and deserialised in the AWS Lambda compute grid.
 */
public class SieveSegmentRequest {

    private List<Integer> smallPrimes;
    private Segment segment;

    public SieveSegmentRequest() {
        // JSON Deserialization
    }

    private SieveSegmentRequest(List<Integer> smallPrimes, Segment segment) {
        this.smallPrimes = smallPrimes;
        this.segment = segment;
    }

    public static SieveSegmentRequest create(List<Integer> smallPrimes, Segment segment) {
        return new SieveSegmentRequest(smallPrimes, segment);
    }

    public List<Integer> getSmallPrimes() {
        return smallPrimes;
    }

    public void setSmallPrimes(List<Integer> smallPrimes) {
        this.smallPrimes = smallPrimes;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SieveSegmentRequest that = (SieveSegmentRequest) o;

        return new EqualsBuilder()
            .append(smallPrimes, that.smallPrimes)
            .append(segment, that.segment)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(smallPrimes)
            .append(segment)
            .toHashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
