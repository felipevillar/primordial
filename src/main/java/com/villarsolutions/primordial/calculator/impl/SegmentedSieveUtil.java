package com.villarsolutions.primordial.calculator.impl;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.List;

public class SegmentedSieveUtil {

    private static final Logger log = LoggerFactory.getLogger(SegmentedSieveUtil.class);

    /**
     * Return all the prime numbers in the given Segment by using the <code>smallPrimes</code>
     * to sieve the multiples of each prime in the segment.
     * <p>
     * Defined as a static method so that we can invoke it from the ParallelEratosthenesSieve
     * as well as from an AWS Lambda.
     */
    public static List<Long> calculatePrimesInSegment(List<Integer> smallPrimes, Segment segment) {
        log.info(String.format("About to process sieve request for segment: %s", segment));
        Stopwatch stopwatch = Stopwatch.createStarted();

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
