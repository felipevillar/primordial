package com.villarsolutions.primordial.calculator.impl;


import com.villarsolutions.primordial.calculator.AbstractPrimeCalculatorTest;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Most of the functionality is tested in the abstract superclass.
 */
public class ParallelSegmentedEratosthenesSieveTest extends AbstractPrimeCalculatorTest {

    private ParallelSegmentedEratosthenesSieve calculator;

    @Before
    public void setUp() throws Exception {
        calculator = new ParallelSegmentedEratosthenesSieve();
    }

    @Override
    protected PrimeCalculator getCalculator() {
        return calculator;
    }

    @Test
    public void testGetNumberOfSegments() throws Exception {
        assertEquals(6, calculator.getNumberOfSegments(24, 4));
        assertEquals(6, calculator.getNumberOfSegments(23, 4));
        assertEquals(6, calculator.getNumberOfSegments(22, 4));
        assertEquals(5, calculator.getNumberOfSegments(21, 4));
    }

    @Test
    public void testGetFinalSegmentSize() throws Exception {
        assertEquals(3, calculator.getFinalSegmentSize(24, 4));
        assertEquals(2, calculator.getFinalSegmentSize(23, 4));
        assertEquals(1, calculator.getFinalSegmentSize(22, 4));
        assertEquals(0, calculator.getFinalSegmentSize(21, 4));
    }
}