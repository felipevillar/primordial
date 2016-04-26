package com.villarsolutions.primordial.calculator.impl;

import com.google.common.base.Stopwatch;
import com.villarsolutions.primordial.exception.CalculationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.villarsolutions.primordial.calculator.impl.SegmentedSieveUtil.calculatePrimesInSegment;


/**
 * Implementation of a segmented Eratosthenes Sieve which uses a fixed thread-pool executor and
 * Java 8 streams to multi-thread the work of sieving each segment.
 * <p>
 * The small primes (from 2 to sqrt(n)) are found first, using the single-threaded
 * Eratosthenes Sieve.  The rest of the number range is the split into segments,
 * and each segment is then processed in parallel using an array sieve.
 * <p>
 * In comparison to the single-threaded Eratosthenes Sieve (which has a limit of <code>Integer.MAX_VALUE</code>),
 * this calculator is able to find much larger primes, up to (MAX_ARRAY_LENGTH ^ 2) - as long as there is enough
 * heap allocated to the process.
 * <p>
 * The fixed thread-pool is initialized with a number of threads equal to the number of available
 * CPU cores.  Therefore the heap footprint will be a function of the available cores.
 *
 * @see Integer#MAX_VALUE
 * @see Runtime#availableProcessors()
 */
@ThreadSafe
public class ParallelEratosthenesSieve extends AbstractSegmentedSieveCalculator {

    private static final Logger log = LoggerFactory.getLogger(ParallelEratosthenesSieve.class);

    public ParallelEratosthenesSieve(int minSegmentSize, int maxSegmentSize, int parallelismLowerBound) {
        super(minSegmentSize, maxSegmentSize, Runtime.getRuntime().availableProcessors(), parallelismLowerBound);
    }

    @Override
    protected List<Long> calculate(long ceiling) throws CalculationException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Segment> segments = getSegments(ceiling);
        List<Integer> smallPrimes = findPrimesInFirstSegment(stopwatch, segments);

        // We now find the bigger primes by calculating each segment in parallel.  Compared to an earlier
        // revision of this class, we now use a fixed thread-pool instead of a parallel stream with the
        // common fork/join pool because it is more optimal to work with fewer (larger) segments.
        // Larger segments allow each core to focus on a segment without the additional context switching
        // and the overhead of the work-stealing algorithm.
        //
        // When testing the Fork/Join Pool vs fixed thread-pool, we found that the fixed threadpool
        // was able to calculate primes up to 2 billion in 58 seconds, versus 85 seconds for the Fork/Join pool.
        //
        // The downside is that if one segment were to take longer to calculate than the others, there is no
        // work-stealing and it would become a bottle-neck.
        ExecutorService executorService = Executors.newFixedThreadPool(getLevelOfParallelism());
        List<Future<List<Long>>> futures = segments.stream()
            .skip(1)
            .map(segment -> executorService.submit(() -> calculatePrimesInSegment(smallPrimes, segment)))
            .collect(Collectors.toList());

        List<Long> result = concatenate(smallPrimes, futures);
        log.info(String.format("Calculation completed. Found [%d] primes overall. Time elapsed = %s", result.size(), stopwatch));
        return result;
    }

    /**
     * Since the first segment is being sieved with a BitSet of size sqrt(n) and
     * each BitSet cannot be longer than <code>Integer.MAX_LENGTH</code>,
     * this algorithm can support a ceiling of <code>Integer.MAX_LENGTH ^ 2</code>
     * <p>
     * Also refer to EratosthenesSieve.getMaxCeilingSupported()
     *
     * @see EratosthenesSieve#getMaxCeilingSupported()
     */
    @Override
    protected Optional<Long> getMaxCeilingSupported() {
        return Optional.of((long) Math.pow(Integer.MAX_VALUE, 2));
    }

}
