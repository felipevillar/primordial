package com.villarsolutions.primordial.calculator;


import com.villarsolutions.primordial.exception.CalculationException;

import java.math.BigInteger;
import java.util.List;

public interface PrimeCalculator {

    /**
     * Returns a list of all the prime numbers up to <code>ceiling</code>.
     * <p>
     * Note that <code>ceiling</code> is inclusive, i.e. the returned list will
     * include the ceiling if it is a prime number.
     */
    List<BigInteger> calculatePrimes(BigInteger ceiling) throws CalculationException;

}
