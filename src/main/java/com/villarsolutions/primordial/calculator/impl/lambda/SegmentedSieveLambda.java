package com.villarsolutions.primordial.calculator.impl.lambda;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.villarsolutions.primordial.calculator.impl.ParallelEratosthenesSieve;
import com.villarsolutions.primordial.calculator.impl.Segment;
import com.villarsolutions.primordial.calculator.impl.aws.AWSLambdaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.BitSet;
import java.util.List;

/**
 * Thread-safe / functional class with a method to return a all the prime numbers in a
 * given <code>Segment</code>. Used by both the ParallelEratosthenesSieve and the
 * AWSLambdaCalculator
 *
 * @see ParallelEratosthenesSieve
 * @see AWSLambdaCalculator
 */
@ThreadSafe
public class SegmentedSieveLambda {

    private static final Logger log = LoggerFactory.getLogger(SegmentedSieveLambda.class);

    /**
     * Return all the prime numbers in the given Segment by using the <code>smallPrimes</code>
     * to sieve the multiples of each prime in the segment.
     * <p>
     * Defined as a static method so that we can invoke it from the ParallelEratosthenesSieve
     * as well as from an AWS Lambda.
     */
    public static List<Long> calculatePrimesInSegment(SieveSegmentRequest request) {
        log.info(String.format("About to process sieve request for segment: %s", request.getSegment()));
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Integer> smallPrimes = request.getSmallPrimes();
        Segment segment = request.getSegment();

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

            for (long index = startIndex; index < sieveLength; index += p) {
                sieve.set((int) index);
            }
        });

        List<Long> primes = Lists.newArrayList();
        for (int index = 0; index < sieveLength; index++) {
            if (!sieve.get(index)) {
                primes.add(lowerBound + index);
            }
        }
        log.info(String.format("Found [%s] primes in segment of size [%s] in %s", primes.size(), segment.getSegmentSize(), stopwatch));
        return primes;
    }

}
