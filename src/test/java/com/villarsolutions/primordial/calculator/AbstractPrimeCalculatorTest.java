package com.villarsolutions.primordial.calculator;

import com.villarsolutions.primordial.PrimordialFixtures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static com.villarsolutions.primordial.PrimordialFixtures.PRIMES_UP_TO_CEILING;
import static org.junit.Assert.*;


public abstract class AbstractPrimeCalculatorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    protected abstract PrimeCalculator getCalculator();

    @Test
    public void whenCeilingIsNegative_thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_BE_GREATER_THAN_1);
        getCalculator().calculatePrimes(-1);
    }

    @Test
    public void whenCeilingIsZero__thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_BE_GREATER_THAN_1);
        getCalculator().calculatePrimes(0);
    }

    @Test
    public void whenCeilingIsOne__thenIllegalArgumentExceptionIsThrown() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(AbstractPrimeCalculator.CEILING_MUST_BE_GREATER_THAN_1);
        getCalculator().calculatePrimes(1);
    }

    @Test
    public void whenCeilingIsTwo_thenASinglePrimeNumberIsFound() throws Exception {
        List<Long> primes = getCalculator().calculatePrimes(2);
        assertNotNull(primes);
        assertEquals(1, primes.size());
        long actual = primes.get(0);
        assertEquals(2, actual);
    }

    @Test
    public void whenCeilingIsSetToCorrectly_thenPrimeNumbersShouldBeGenerated() throws Exception {
        List<Long> primes = getCalculator().calculatePrimes(PrimordialFixtures.CEILING);
        assertNotNull(primes);
        assertFalse(primes.isEmpty());
        assertEquals(PRIMES_UP_TO_CEILING, primes);
    }

    @Test
    public void whenCeilingIsSetToAPrimeNumber_thenThisPrimeNumbersIsInResult() throws Exception {
        List<Long> primes = getCalculator().calculatePrimes(19);
        assertNotNull(primes);
        assertFalse(primes.isEmpty());
        long actual = primes.get(primes.size()-1);
        assertEquals(19L, actual);
    }

}
