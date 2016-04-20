package com.villarsolutions.primordial.calculator.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.villarsolutions.primordial.calculator.AbstractPrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Single-threaded implementation of Eratosthenes Sieve.
 * <p>
 * Only a ceiling as high as <code>Integer.MAX_VALUE</code> is supported for this algorithm
 * because it uses a BitSet to sieve the numbers.
 * <p>
 * Also note that multiple requests running concurrently in the same JVM will contend for heap space,
 * so the available memory will also limit the actual ceiling that can be used.
 * <p>
 * The time complexity of this algorithm is O( n log log n )
 *
 * @see Integer#MAX_VALUE
 */
public class EratosthenesSieve extends AbstractPrimeCalculator {

    @Override
    protected List<Long> calculate(long ceiling) throws CalculationException {
        Preconditions.checkArgument(ceiling <= Integer.MAX_VALUE);
        return findPrimes((int) ceiling).stream()
                .mapToLong(i -> (long) i)
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * This method is public and static so that this sieving logic can be reused by other calculators.
     *
     * @return an ImmutableList with the prime numbers from 2 to ceiling.
     */
    public static ImmutableList<Integer> findPrimes(int ceiling) {
        if (ceiling < 2) {
            return ImmutableList.of();
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

        ImmutableList.Builder<Integer> results = ImmutableList.builder();
        for (long n = 2; n <= ceiling; n++) {
            int intN = (int) n;
            if (isPrime(intN, sieve)) {
                results.add(intN);
            }
        }

        return results.build();
    }

    private static boolean isPrime(int n, BitSet sieve) {
        return !sieve.get(n - 2);
    }

    @Override
    protected Optional<Long> getMaxCeilingSupported() {
        return Optional.of((long) Integer.MAX_VALUE);
    }

}

