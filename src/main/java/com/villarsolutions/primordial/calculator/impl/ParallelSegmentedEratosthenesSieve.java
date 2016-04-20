package com.villarsolutions.primordial.calculator.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
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
 * In comparison to the single-threaded Eratosthenes Sieve (which has a limit of <code>Integer.MAX_VALUE</code>),
 * this calculator is able to find much larger primes, up to (MAX_ARRAY_LENGTH ^ 2) - as long as there is enough
 * heap allocated to the process.
 * <p>
 * Since the multi-threading is done with a parallel stream which uses a default ForkJoinPool,
 * the actual number of threads used will default to the number of CPU cores.
 * Therefore the heap footprint will be a function of the available cores.
 *
 * @see Integer#MAX_VALUE
 * @see Runtime#availableProcessors()
 * @see java.util.concurrent.ForkJoinPool
 */
public class ParallelSegmentedEratosthenesSieve extends AbstractPrimeCalculator {

    private static final Logger log = LoggerFactory.getLogger(AbstractPrimeCalculator.class);

    @Override
    protected List<Long> calculate(long ceiling) throws CalculationException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        // Note: It safe to cast the square root of ceiling to an int because we limit the
        // range of the ceiling using the getMaxCeilingSupported() method.
        // The range of 'ceiling' is validated in the superclass so no need to check it again here.
        int segmentSize = (int) Math.sqrt(ceiling);

        // Find the primes in the first segment.  Note that the range we are segmenting
        // is from 2 to ceiling, so the first segment spans the numbers from 2 to "segmentSize + 1"
        ImmutableList<Integer> smallPrimes = EratosthenesSieve.findPrimes(segmentSize + 1);
        log.debug(String.format("Found [%d] small primes from 2 to [%d]. Time elapsed = %s",
                smallPrimes.size(), segmentSize + 1, stopwatch));

        // Now set up the remaining segments.
        List<Segment> segments = Lists.newArrayList();
        long numberOfSegments = getNumberOfSegments(ceiling, segmentSize);

        // The segmentNumbers start at 0, and we can skip the first segment.
        for (long segmentNumber = 1; segmentNumber < (numberOfSegments - 1); segmentNumber++) {
            long lowerBound = (segmentNumber * segmentSize) + 2;
            segments.add(new Segment(lowerBound, segmentSize));
        }

        // Now the final segment
        long lowerBound = (numberOfSegments-1) * segmentSize + 2;
        int finalSegmentSize = getFinalSegmentSize(ceiling, segmentSize);
        segments.add(new Segment(lowerBound, finalSegmentSize));

        log.debug(String.format("Created [%d] segments of size [%d] and a final segment of size [%d]. Time elapsed = %s",
                segments.size(), segmentSize, finalSegmentSize, stopwatch));

        // Concatenate the smaller primes with the rest of the primes in the number line.
        // The bigger primes are calculated in parallel using a parallelStream.
        // Each thread in the fork/join pool is given a segment to work on independently.
        // The flatMap operation combines the results of the parallel work into
        // a single stream, so that it can then be concatenated with the stream of small primes.
        List<Long> result = Stream.concat(
                smallPrimes.stream().mapToLong(i -> (long) i).boxed(),
                segments.parallelStream()
                        .map(s -> calculatePrimesInSegment(smallPrimes, s))
                        .flatMap(Collection::stream))
                .collect(Collectors.toList());

        log.debug(String.format("Calculation completed. Found [%d] primes overall. Time elapsed = %s", result.size(), stopwatch));
        return result;
    }

    private List<Long> calculatePrimesInSegment(ImmutableList<Integer> smallPrimes, Segment segment) {
        // false means the number is prime.  This is the same convention
        // that was used in the basic EratosthenesSieve
        BitSet sieve = new BitSet(segment.getSegmentSize());
        int sieveLength = segment.getSegmentSize();
        long lowerBound = segment.getLowerBound();

        // For each small prime 'p', eliminate the multiples of p from the sieve
        smallPrimes.forEach(p -> {
            // This a safe-cast to int because p is an int, so the % operation
            // yields a remainder that is < Integer.MAX_VALUE
            int remainder = (int) (lowerBound % p);
            int startIndex = remainder == 0 ? 0 : (p - remainder);

            for (int index = startIndex; index < sieveLength; index += p) {
                sieve.set(index);
            }
        });

        ImmutableList.Builder<Long> results = ImmutableList.builder();
        for (int index = 0; index < sieveLength; index++) {
            if (!sieve.get(index)) {
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
     * We are segmenting the range into multiple BitSets of size sqrt(n) and
     * each BitSet cannot be longer than <code>Integer.MAX_LENGTH</code>.
     * Thus, this algorithm can support a ceiling of <code>Integer.MAX_LENGTH ^ 2</code>
     * <p>
     * Also refer to EratosthenesSieve.getMaxCeilingSupported()
     *
     * @see EratosthenesSieve#getMaxCeilingSupported()
     */
    @Override
    protected Optional<Long> getMaxCeilingSupported() {
        return Optional.of((long) Math.pow(Integer.MAX_VALUE, 2));
    }

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
