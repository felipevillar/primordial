package com.villarsolutions.primordial.calculator.impl;

import com.google.common.base.Preconditions;
import com.villarsolutions.primordial.exception.CalculationException;

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
public class EratosthenesSieve extends AbstractSieveCalculator {

    @Override
    protected List<Long> calculate(long ceiling) throws CalculationException {
        Preconditions.checkArgument(ceiling <= Integer.MAX_VALUE);
        return findPrimes((int) ceiling).stream()
                .mapToLong(i -> (long) i)
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    protected Optional<Long> getMaxCeilingSupported() {
        return Optional.of((long) Integer.MAX_VALUE);
    }

}

