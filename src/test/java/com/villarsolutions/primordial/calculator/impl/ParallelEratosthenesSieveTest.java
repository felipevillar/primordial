package com.villarsolutions.primordial.calculator.impl;


import com.villarsolutions.primordial.calculator.AbstractPrimeCalculatorTest;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import org.junit.Before;

/**
 * The functionality is tested in the abstract superclass.
 */
public class ParallelEratosthenesSieveTest extends AbstractPrimeCalculatorTest {

    private ParallelEratosthenesSieve calculator;

    @Before
    public void setUp() throws Exception {
        calculator = new ParallelEratosthenesSieve(1, Integer.MAX_VALUE, 19);
    }

    @Override
    protected PrimeCalculator getCalculator() {
        return calculator;
    }



}