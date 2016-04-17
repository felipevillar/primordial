package com.villarsolutions.primordial.calculator.impl;

import com.google.common.collect.Lists;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.villarsolutions.primordial.util.PrimordialUtil.*;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Naive & slow implementation of PrimeCalculator which uses trial division
 * to test each number in the range for primality.  For every number N being tested,
 * it only needs to search from 2 to sqrt(N) so the time complexity of this algorithm is O( n*sqrt(n) )
 * <p>
 * This implementation uses BigIntegers, which although extremely slow, do have the benefit of not being bounded.
 * <p>
 * For a faster and more reasonable implementation please refer to <code>EratosthenesSieve</code>
 * or <code>ParallelSegmentedEratosthenesSieve</code>
 */
public class BasicTrialDivision extends AbstractPrimeCalculator {

    @Override
    protected List<BigInteger> calculate(BigInteger ceiling) throws CalculationException {
        if (ceiling.compareTo(TWO) < 0) {
            return Lists.newArrayList();
        }

        List<BigInteger> primes = Lists.newArrayList();
        for (BigInteger n = TWO;
             n.compareTo(ceiling) <= 0;
             n = n.add(ONE)) {

            if (isPrime(n)) {
                primes.add(n);
            }
        }
        return primes;
    }

    private static boolean isPrime(BigInteger n) {
        // Assume n is prime until proven otherwise.
        boolean prime = true;

        // Note that the number 2 is prime
        int c = n.compareTo(TWO);

        if (c < 0) {
            // any integer < 2 is not prime
            prime =  false;
        } else if (c > 0) {
            if (isEven(n)) {
                prime = false;
            } else {
                // for p in (2 to sqrt(n)) by increments of 1
                // if p divides n with no remainder, then n is not prime
                for (BigInteger p = TWO;
                     p.multiply(p).compareTo(n) <= 0;
                     p = p.add(ONE)) {

                    if (n.mod(p).compareTo(ZERO) == 0) {
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
    protected Optional<BigInteger> getMaxCeilingSupported() {
        return Optional.empty();
    }
}
