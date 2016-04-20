package com.villarsolutions.primordial.calculator.impl;

import com.villarsolutions.primordial.calculator.AbstractPrimeCalculatorTest;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Most of the functionality is tested in the abstract superclass.
 */
public class EratosthenesSieveTest extends AbstractPrimeCalculatorTest {

    private PrimeCalculator calculator;

    @Before
    public void setUp() throws Exception {
        calculator = new EratosthenesSieve();
    }

    @Override
    protected PrimeCalculator getCalculator() {
        return calculator;
    }

    @Test
    public void whenCeilingIsAboveIntegerMaxValue_thenCalculationExceptionIsThrown() throws Exception {
        exception.expect(CalculationException.class);
        exception.expectMessage("This calculator only supports prime numbers up to [");
        assertThat(getCalculator().calculatePrimes((long) Integer.MAX_VALUE + 1));
    }

}