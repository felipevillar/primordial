package com.villarsolutions.primordial.calculator;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.villarsolutions.primordial.exception.CalculationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.villarsolutions.primordial.util.PrimordialUtil.getDecimalFormat;
import static java.math.BigInteger.ONE;

/**
 * Abstract calculator that:
 * <ul>
 *     <li>Validates parameters and verifies that the ceiling is below the limit set by each calculator.</li>
 *     <li>Uses a Stopwatch to log the computation time.</li>
 *     <li>Wraps RuntimeExceptions into CalculationExceptions so that they can be handled gracefully by client code.</li>
 * </ul>
 */
public abstract class AbstractPrimeCalculator implements PrimeCalculator {

    private static final Logger log = LoggerFactory.getLogger(AbstractPrimeCalculator.class);

    public static final String CEILING_MUST_NOT_BE_NULL = "ceiling must not be null";
    public static final String CEILING_MUST_BE_GREATER_THAN_1 = "ceiling must be greater than 1";

    @Override
    public List<BigInteger> calculatePrimes(BigInteger ceiling) throws CalculationException {
        Preconditions.checkArgument(ceiling != null, CEILING_MUST_NOT_BE_NULL);
        Preconditions.checkArgument(ceiling.compareTo(ONE) > 0, CEILING_MUST_BE_GREATER_THAN_1);
        validateCeiling(ceiling);

        try {
            log.info(String.format("Calculating prime numbers using calculator [%s] up to ceiling [%s]", getClass().getSimpleName(), getDecimalFormat().format(ceiling)));
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<BigInteger> result = calculate(ceiling);
            log.info(String.format("[%s] completed the calculation in [%s].  Found [%s] prime numbers up to ceiling [%s]", getClass().getSimpleName(), stopwatch, result.size(), getDecimalFormat().format(ceiling)));
            return result;
        } catch (RuntimeException e) {
            String msg = String.format("Calculator [%s] could not complete due to RuntimeException.", getClass().getSimpleName());
            log.error(msg, e);
            throw new CalculationException(msg, e);
        }
    }

    protected abstract List<BigInteger> calculate(BigInteger ceiling) throws CalculationException;

    /**
     * The maximum ceiling number supported by the calculator's algorithm.
     * <p>
     * If not set, then it is assumed that any ceiling value is supported
     * and the computation will likely then be limited by either compute time or memory.
     */
    protected abstract Optional<BigInteger> getMaxCeilingSupported();

    private void validateCeiling(BigInteger ceiling) throws CalculationException {
        Optional<BigInteger> ceilingBound = getMaxCeilingSupported();
        ceilingBound.ifPresent(maxCeiling -> {
            if (ceiling.compareTo(maxCeiling) >= 0) {
                throw new CalculationException(String.format("This calculator only supports prime numbers less than [%s]", getDecimalFormat().format(maxCeiling)));
            }
        });
    }

}
