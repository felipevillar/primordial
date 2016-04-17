package com.villarsolutions.primordial.exception;

/**
 * Indicates that an unexpected error prevented a PrimeCalculator
 * from completing a calculation properly.
 *
 * @see com.villarsolutions.primordial.calculator.PrimeCalculator
 */
public class CalculationException extends RuntimeException {

    public CalculationException(String message) {
        super(message);
    }

    public CalculationException(String message, Throwable cause) {
        super(message, cause);
    }

}
