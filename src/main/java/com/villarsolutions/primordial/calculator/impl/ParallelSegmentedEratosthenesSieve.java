package com.villarsolutions.primordial.calculator.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;
import com.villarsolutions.primordial.util.PrimordialUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of a segmented Eratosthenes Sieve which uses Java 8 parallel streams
 * to multi-thread the work of sieving each segment.
 * <p>
 * The small primes (from 2 to sqrt(n)) are found first, using the single-threaded
 * Eratosthenes Sieve.  The rest of the number range is the split into segments,
 * and each segment is then processed in parallel using an array sieve.
 * <p>
 * In comparison to the single-threaded Eratosthenes Sieve (which has a limit of MAX_ARRAY_LENGTH),
 * this calculator is able to find much larger primes, up to (MAX_ARRAY_LENGTH ^ 2) - as long as there is enough
 * heap allocated to the process.
 *
 * @see PrimordialUtil#MAX_ARRAY_LENGTH
 */
public class ParallelSegmentedEratosthenesSieve extends AbstractPrimeCalculator {

    @Override
    protected List<BigInteger> calculate(BigInteger ceiling) throws CalculationException {
        // Note it is safe to convert ceiling to a long, because we limit the range of the ceiling
        // using the getMaxCeilingSupported() method. For the same reason, it safe to cast the
        // square root of ceiling to an int.
        // The range of 'ceiling' is validated in the superclass so no need to check it again here.
        long ceilingL = ceiling.longValue();
        int segmentSize = (int) Math.sqrt(ceilingL);

        // Find the primes in the first segment.  Note that the range we are segmenting
        // is from 2 to ceiling, so the first segment spans the numbers from 2 to "segmentSize + 1"
        List<Integer> smallPrimes = EratosthenesSieve.findPrimes(segmentSize + 1);

        // Now set up the remaining segments.
        List<Segment> segments = Lists.newArrayList();
        long numberOfSegments = getNumberOfSegments(ceilingL, segmentSize);

        // The segmentNumbers start at 0, and we can skip the first segment.
        for (long segmentNumber = 1; segmentNumber < (numberOfSegments - 1); segmentNumber++) {
            long lowerBound = (segmentNumber * segmentSize) + 2;
            segments.add(new Segment(lowerBound, segmentSize));
        }

        // Now the final segment
        long lowerBound = (numberOfSegments-1) * segmentSize + 2;
        int finalSegmentSize = getFinalSegmentSize(ceilingL, segmentSize);
        segments.add(new Segment(lowerBound, finalSegmentSize));

        List<Long> biggerPrimes =
                segments.parallelStream()
                .map(s -> calculatePrimesInSegment(smallPrimes, s))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Concatenate the smaller primes with the rest of the primes found in the segments.
        return Stream.concat(
                smallPrimes.stream().map(BigInteger::valueOf),
                biggerPrimes.stream().map(BigInteger::valueOf))
                .collect(Collectors.toList());
    }

    private List<Long> calculatePrimesInSegment(List<Integer> smallPrimes, Segment segment) {
        // false means the number is prime.  This is the same convention
        // that was used in the basic EratosthenesSieve
        boolean[] sieve = new boolean[segment.getSegmentSize()];
        long lowerBound = segment.getLowerBound();

        smallPrimes.forEach(p -> {
            // Eliminate the multiples of p from the sieve
            int remainder = (int) (lowerBound % p);
            int startIndex = remainder == 0 ? 0 : (p - remainder);

            for (int index = startIndex; index < sieve.length; index += p) {
                sieve[index] = true;
            }
        });

        ImmutableList.Builder<Long> results = ImmutableList.builder();
        for (int index = 0; index < sieve.length; index++) {
            if (!sieve[index]) {
                results.add(lowerBound + index);
            }
        }

        return results.build();

    }

    @VisibleForTesting
    protected int getFinalSegmentSize(long ceiling, int segmentSize) {
        long finalSegmentSize = (ceiling - 1) % segmentSize;
        return (int) finalSegmentSize;
    }

    @VisibleForTesting
    protected long getNumberOfSegments(long ceiling, int segmentSize) {
        double rangeSize = ceiling - 1;
        return (long) Math.ceil(rangeSize / segmentSize);
    }

    /**
     * As we are segmenting the range into multiple arrays of size sqrt(n) and
     * each array cannot be longer than <code>MAX_ARRAY_LENGTH</code>,
     * then this algorithm can support a ceiling of <code>MAX_ARRAY_LENGTH ^ 2</code>
     * <p>
     * Also refer to EratosthenesSieve.getMaxCeilingSupported()
     *
     * @see EratosthenesSieve#getMaxCeilingSupported()
     * @see PrimordialUtil#MAX_ARRAY_LENGTH
     */
    @Override
    protected Optional<BigInteger> getMaxCeilingSupported() {
        return Optional.of(PrimordialUtil.MAX_ARRAY_LENGTH.pow(2));
    }


    private static class Segment {

        /**
         * The first number included in this segment. For example,
         * if this segment represents an inclusive range of numbers
         * from 6 to 9, the lowerBound is 6
         */
        private final long lowerBound;

        /**
         * The number of numbers included in this segment.  For example,
         * if this segment represents the inclusive range from 6 to 9,
         * the segmentSize is 4.
         * <p>
         * Segments are equally sized apart from the last segment which
         * might be smaller than the other ones.
         */
        private final int segmentSize;

        public Segment(long lowerBound, int segmentSize) {
            this.lowerBound = lowerBound;
            this.segmentSize = segmentSize;
        }

        public long getLowerBound() {
            return lowerBound;
        }

        public int getSegmentSize() {
            return segmentSize;
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
            return ReflectionToStringBuilder.toString(this);
        }
    }

}
