package com.villarsolutions.primordial.calculator;

import com.villarsolutions.primordial.PrimordialFixtures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.List;

import static com.villarsolutions.primordial.PrimordialFixtures.PRIMES_UP_TO_CEILING;
import static com.villarsolutions.primordial.util.PrimordialUtil.TWO;
import static com.villarsolutions.primordial.util.PrimordialUtil.bigInt;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static org.junit.Assert.*;


public abstract class AbstractPrimeCalculatorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    protected abstract PrimeCalculator getCalculator();
    
    @Test
    public void whenCeilingIsNull_thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_NOT_BE_NULL);
        getCalculator().calculatePrimes(null);
    }

    @Test
    public void whenCeilingIsNegative_thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_BE_GREATER_THAN_1);
        getCalculator().calculatePrimes(bigInt(-1));
    }

    @Test
    public void whenCeilingIsZero__thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_BE_GREATER_THAN_1);
        getCalculator().calculatePrimes(ZERO);
    }

    @Test
    public void whenCeilingIsOne__thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_BE_GREATER_THAN_1);
        getCalculator().calculatePrimes(ONE);
    }

    @Test
    public void whenCeilingIsTwo_thenASinglePrimeNumberIsFound() throws Exception {
        List<BigInteger> primes = getCalculator().calculatePrimes(TWO);
        assertNotNull(primes);
        assertEquals(1, primes.size());
        assertEquals(TWO, primes.get(0));
    }

    @Test
    public void whenCeilingIsSetToCorrectly_thenPrimeNumbersShouldBeGenerated() throws Exception {
        List<BigInteger> primes = getCalculator().calculatePrimes(PrimordialFixtures.CEILING);
        assertNotNull(primes);
        assertFalse(primes.isEmpty());
        assertEquals(PRIMES_UP_TO_CEILING, primes);
    }

    @Test
    public void whenCeilingIsSetToAPrimeNumber_thenThisPrimeNumbersIsInResult() throws Exception {
        BigInteger NINETEEN = bigInt(19);
        List<BigInteger> primes = getCalculator().calculatePrimes(NINETEEN);
        assertNotNull(primes);
        assertFalse(primes.isEmpty());
        assertEquals(NINETEEN, primes.get(primes.size()-1));
    }

}
