package com.villarsolutions.primordial.calculator.impl;

import com.google.common.collect.Lists;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;

import java.util.List;
import java.util.Optional;

import static com.villarsolutions.primordial.util.PrimordialUtil.isEven;

/**
 * Naive and slow implementation of PrimeCalculator which uses trial division to test
 * each number in the range for primality.  For every number N being tested, it only needs to
 * search from 2 to sqrt(N) so the time complexity of this algorithm is O( n*sqrt(n) )
 * <p>
 * For a faster and more reasonable implementation refer to <code>EratosthenesSieve</code>
 * or <code>ParallelSegmentedEratosthenesSieve</code>
 */
public class BasicTrialDivision extends AbstractPrimeCalculator {

    @Override
    protected List<Long> calculate(long ceiling) throws CalculationException {
        if (ceiling < 2) {
            return Lists.newArrayList();
        }

        List<Long> primes = Lists.newArrayList();
        for (long n = 2; n <= ceiling; n++) {
            if (isPrime(n)) {
                primes.add(n);
            }
        }
        return primes;
    }

    private static boolean isPrime(long n) {
        // Assume n is prime until proven otherwise.
        boolean prime = true;

        // Note that the number 2 is prime
        if (n < 2) {
            // any integer < 2 is not prime
            prime =  false;
        } else if (n > 2) {
            if (isEven(n)) {
                prime = false;
            } else {
                // for p in (2 to sqrt(n)) by increments of 1
                // if p divides n with no remainder, then n is not prime
                for (long p = 2; p * p <= n; p++) {
                    if (n % p == 0) {
                        // p divides n perfectly, therefore n is not prime
                        prime = false;
                        break;
                    }
                }
            }
        }

        return prime;
    }

    @Override
    protected Optional<Long> getMaxCeilingSupported() {
        return Optional.of(Long.MAX_VALUE);
    }
}
