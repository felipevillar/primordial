package com.villarsolutions.primordial.calculator.impl;

import com.villarsolutions.primordial.calculator.AbstractPrimeCalculatorTest;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import org.junit.Before;

/**
 * Most of the functionality is tested in the abstract superclass.
 */
public class BasicTrialDivisionTest extends AbstractPrimeCalculatorTest {

    private PrimeCalculator calculator;

    @Before
    public void setUp() throws Exception {
        calculator = new BasicTrialDivision();
    }

    @Override
    protected PrimeCalculator getCalculator() {
        return calculator;
    }

}