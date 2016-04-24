package com.villarsolutions.primordial.calculator.impl;

import com.google.common.collect.Lists;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;

import java.util.BitSet;
import java.util.List;

/**
 * Shared functionality across all sieve calculators.
 */
public abstract class AbstractSieveCalculator extends AbstractPrimeCalculator {

    /**
     * This method is able to find primes up to Integer.MAX_VALUE using a single-threaded sieve.
     *
     * @param  ceiling (inclusive) maximum number for which to find primes.
     * @return an ImmutableList with the prime numbers from 2 to ceiling.
     */
    protected List<Integer> findPrimes(int ceiling) {
        if (ceiling < 2) {
            return Lists.newArrayList();
        }

        // The BitSet sieve contains a bit for each number
        // from 2 to ceiling. If set, it means the number is *not* prime.
        // We use this negated definition instead of saying "a set bit means prime" to avoid
        // having to initialize the whole BitSet with true values.
        int sieveLength = ceiling - 1;
        BitSet sieve = new BitSet(sieveLength);

        // We use 'long' in these for loops to guard against integer overflow.
        // However, because ceiling is a positive int, it is safe to cast back
        // to an int inside the loop.
        for (long n = 2; n * n < ceiling; n++) {
            if (isPrime((int) n, sieve)) {
                for (long j = n * n; j <= ceiling; j += n) {
                    sieve.set((int) j - 2);
                }
            }
        }

        List<Integer> results = Lists.newArrayList();
        for (long n = 2; n <= ceiling; n++) {
            int intN = (int) n;
            if (isPrime(intN, sieve)) {
                results.add(intN);
            }
        }

        return results;
    }

    private static boolean isPrime(int n, BitSet sieve) {
        return !sieve.get(n - 2);
    }

}
