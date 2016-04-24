package com.villarsolutions.primordial.calculator.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.villarsolutions.primordial.util.PrimordialUtil.getDecimalFormat;

/**
 * Abstract class used by calculators with a parallel segmented "sieve of Eratosthenes" algorithm.
 * This class mostly contains logic to split a number range into segments.
 */
public abstract class AbstractSegmentedSieveCalculator extends AbstractSieveCalculator {

    private static final Logger log = LoggerFactory.getLogger(AbstractSegmentedSieveCalculator.class);

    private final int minSegmentSize;
    private final int maxSegmentSize;
    private final int levelOfParallelism;
    private final int parallelismLowerBound;

    public AbstractSegmentedSieveCalculator(int minSegmentSize, int maxSegmentSize, int levelOfParallelism, int parallelismLowerBound) {
        this.minSegmentSize = minSegmentSize;
        this.maxSegmentSize = maxSegmentSize;
        this.levelOfParallelism = levelOfParallelism;
        this.parallelismLowerBound = parallelismLowerBound;
    }

    protected int getMinSegmentSize() {
        return minSegmentSize;
    }

    protected int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    protected int getLevelOfParallelism() {
        return levelOfParallelism;
    }

    private int getParallelismLowerBound() {
        return parallelismLowerBound;
    }

    protected List<Segment> getSegments(long ceiling) {
        return divideIntoSegments(ceiling, getMinSegmentSize(), getMaxSegmentSize(), getLevelOfParallelism(), getParallelismLowerBound());
    }

    /**
     * Split the number line into multiple segments depending on the given
     * <code>levelOfParallelism</code>
     * <p>
     * The following rules are observed:
     * <ul>
     *     <li>Since 2 is the smallest prime, the first segment always starts at 2</li>
     *     <li>If the ceiling is <= <code>parallelismLowerBound</code>, then we will only create a single
     *         segment spanning the entire range from 2 to ceiling.</li>
     *     <li>If the ceiling is > <code>parallelismLowerBound</code>, then the first segment will always span
     *         the range: 2 to sqrt(ceiling)</li>
     *     <li>The remaining segments will be split evenly according to the logic in <code>getSegmentSize</code>
     *         but the final segment might be smaller than the others to capture the remainder.</li>
     * </ul>
     *
     * @param parallelismLowerBound a number below which we will not do any work in parallel. For small number ranges
     *                              a single-threaded sieve is faster, so for optimal performance, this lower
     *                              bound should be tuned.
     * @param levelOfParallelism The number of threads (or lambdas) that will be able to compute segments in parallel.
     */
    @VisibleForTesting
    protected static List<Segment> divideIntoSegments(long ceiling,
                                                      int minSegmentSize,
                                                      int maxSegmentSize,
                                                      int levelOfParallelism,
                                                      int parallelismLowerBound) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ImmutableList.Builder<Segment> builder = ImmutableList.builder();
        if (ceiling <= parallelismLowerBound) {
            builder.add(Segment.create(2, (int) ceiling - 1));
        } else {
            // Note: It safe to cast the square root of ceiling to an int because we limit the
            // range of the ceiling using the getMaxCeilingSupported() method.
            // The range of 'ceiling' is validated in the superclass so no need to check it again here.
            int squareRootFloor = (int) Math.sqrt(ceiling);
            Segment s1 = Segment.create(2, squareRootFloor-1);
            long remainingRange = ceiling - s1.getSegmentSize() - 1;
            int segmentSize = getSegmentSize(minSegmentSize, maxSegmentSize, levelOfParallelism, remainingRange);
            List<Segment> remainingSegments = getRemainingSegments(s1, remainingRange, segmentSize);

            builder.add(s1);
            builder.addAll(remainingSegments);
        }

        ImmutableList<Segment> segments = builder.build();
        logSegmentSizes(stopwatch, segments);

        return segments;
    }

    /**
     * Returns the segment size to use depending on the given parameters.  The segment size will be
     * set by splitting the numberRange evenly according to the <code>levelOfParallelism</code>.
     * <p>
     * The segment size will never be smaller than <code>minSegmentSize</code> or larger than <code>maxSegmentSize</code>
     * <p>
     * The segments size is also capped by Integer.MAX_VALUE
     *
     * @param minSegmentSize this is used so that segment sizes don't get too small. If segments
     *                       are too small it is not worth incurring the overhead of parallelism.
     * @param maxSegmentSize this is used so that segments do not exceed limits such as the AWS limit on response
     *                       body payloads.
     */
    private static int getSegmentSize(int minSegmentSize, int maxSegmentSize, int levelOfParallelism, long numberRange) {
        long suggestedSize = (long) Math.ceil((double) numberRange / levelOfParallelism);
        int segmentSize;
        if (suggestedSize < minSegmentSize) {
            segmentSize = minSegmentSize;
        } else if (suggestedSize > maxSegmentSize) {
            segmentSize = maxSegmentSize;
        } else if (suggestedSize > Integer.MAX_VALUE) {
            segmentSize = Integer.MAX_VALUE;
        } else {
            segmentSize = (int) suggestedSize;
        }
        return (int) Math.min(segmentSize, numberRange);
    }

    private static List<Segment> getRemainingSegments(Segment firstSegment, long remainingRange, int segmentSize) {
        List<Segment> remainingSegments = Lists.newArrayList();
        long numRemainingSegments = (long) Math.ceil((double) remainingRange / segmentSize);
        for (long segmentNumber = 1; segmentNumber <= numRemainingSegments; segmentNumber++) {
            long lowerBound = 2 + firstSegment.getSegmentSize() + ((segmentNumber-1) * segmentSize);
            if (segmentNumber < numRemainingSegments) {
                remainingSegments.add(Segment.create(lowerBound, segmentSize));
            } else {
                // Final segment
                int remainder = (int) (remainingRange % segmentSize);
                int finalSegmentSize = remainder == 0 ? segmentSize : remainder;
                remainingSegments.add(Segment.create(lowerBound, finalSegmentSize));
            }
        }
        return remainingSegments;
    }


    private static void logSegmentSizes(Stopwatch stopwatch, ImmutableList<Segment> segments) {
        int numSegments = segments.size();
        int firstSegmentSize = segments.get(0).getSegmentSize();
        if (numSegments == 1) {
            log.info(String.format("Created one segment of size [%s]. Time elapsed = %s", getDecimalFormat().format(firstSegmentSize), stopwatch));
        } else if (numSegments == 2) {
            int secondSegmentSize = segments.get(1).getSegmentSize();
            log.info(String.format("Created two segments. First segment size = [%s]. Second segment size = [%s]. Time elapsed = %s",
                firstSegmentSize, getDecimalFormat().format(secondSegmentSize), stopwatch));
        } else {
            int secondSegmentSize = segments.get(1).getSegmentSize();
            int finalSegmentSize = segments.get(segments.size() - 1).getSegmentSize();
            log.info(String.format("Created [%d] segments. First segment size = [%s]. Middle segments size = [%s]. Final segment size = [%s]. Time elapsed = %s",
                segments.size(), getDecimalFormat().format(firstSegmentSize), getDecimalFormat().format(secondSegmentSize), getDecimalFormat().format(finalSegmentSize), stopwatch));
        }
    }


    protected List<Integer> findPrimesInFirstSegment(Stopwatch stopwatch, List<Segment> segments) {
        int upperBound = (int) segments.get(0).getUpperBound();
        List<Integer> smallPrimes = findPrimes(upperBound);
        log.info(String.format("Found [%d] small primes from 2 to [%d]. Time elapsed = %s", smallPrimes.size(), upperBound, stopwatch));
        return smallPrimes;
    }

    protected List<Long> getPrimesFromFuture(Future<List<Long>> f) {
        try {
            return f.get();
        } catch (Exception e) {
            log.error("Error retrieving result of computation from segment.", e);
            throw Throwables.propagate(e);
        }
    }

    /**
     * Concatenate the smaller primes with the rest of the primes in the number line.
     * The flatMap operation combines the results of the work that was computed in parallel into
     * a single stream, so that it can then be concatenated with the stream of small primes.
     */
    protected List<Long> concatenate(List<Integer> smallPrimes, List<Future<List<Long>>> futures) {
        return Stream.concat(
            smallPrimes.stream().mapToLong(i -> (long) i).boxed(),
            futures.stream()
                .map(this::getPrimesFromFuture)
                .flatMap(Collection::stream))
            .collect(Collectors.toList());
    }
}
