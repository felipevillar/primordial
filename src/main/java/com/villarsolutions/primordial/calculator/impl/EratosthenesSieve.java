package com.villarsolutions.primordial.calculator.impl;

import com.google.common.collect.ImmutableList;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;
import com.villarsolutions.primordial.util.PrimordialUtil;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.villarsolutions.primordial.util.PrimordialUtil.toBigInts;

/**
 * Single-threaded implementation of Eratosthenes Sieve.
 * <p>
 * Only a ceiling as high as <code>MAX_ARRAY_LENGTH</code> is supported for this algorithm
 * because it uses an array to sieve the numbers.
 * <p>
 * Also note that multiple requests running concurrently in the same JVM will contend for heap space,
 * so this will also limit the actual ceiling that can be used.
 * <p>
 * The time complexity of this algorithm is O( n log log n )
 *
 * @see PrimordialUtil#MAX_ARRAY_LENGTH
 */
public class EratosthenesSieve extends AbstractPrimeCalculator {

    @Override
    protected List<BigInteger> calculate(BigInteger ceiling) throws CalculationException {
        return toBigInts(findPrimes(ceiling.intValue()));
    }

    /**
     * This method is public and static so that this sieving logic can be reused by other calculators.
     *
     * @return an ImmutableList with the prime numbers from 2 to ceiling.
     */
    public static List<Integer> findPrimes(int ceiling) {
        if (ceiling < 2) {
            return ImmutableList.of();
        }

        // The sieve array contains a flag for each number
        // from 2 to ceiling. If true, it means the number is *not* prime.
        // We use this "double-negative" definition instead of saying "true is prime" to avoid
        // having to initialize the whole array with true values.
        boolean[] sieve = new boolean[ceiling - 1];

        for (int n = 2; n * n < ceiling; n++) {
            if (isPrime(n, sieve)) {
                for (int j = n * n;
                     j <= ceiling;
                     j += n) {

                    sieve[j - 2] = true;
                }
            }
        }

        ImmutableList.Builder<Integer> results = ImmutableList.builder();
        for (int n = 2; n < sieve.length + 2; n++) {
            if (isPrime(n, sieve)) {
                results.add(n);
            }
        }

        return results.build();
    }

    private static boolean isPrime(int n, boolean[] sieve) {
        return !sieve[n - 2];
    }

    @Override
    protected Optional<BigInteger> getMaxCeilingSupported() {
        return Optional.of(PrimordialUtil.MAX_ARRAY_LENGTH);
    }

}
