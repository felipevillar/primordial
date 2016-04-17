package com.villarsolutions.primordial.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class CalculationResult {

    private String calculatorType;
    private BigInteger ceiling;
    private int countOfPrimes;
    private Integer keepLast;
    private BigDecimal timeElapsedSecs;
    private String timeElapsedDesc;
    private List<BigInteger> primes;

    public CalculationResult() {
        // Jackson deserialization
    }

    private CalculationResult(String calculatorType, BigInteger ceiling, int countOfPrimes, Integer keepLast,
                              List<BigInteger> primes, BigDecimal timeElapsedSecs, String timeElapsedDesc) {
        this.calculatorType = calculatorType;
        this.ceiling = ceiling;
        this.countOfPrimes = countOfPrimes;
        this.keepLast = keepLast;
        this.primes = primes;
        this.timeElapsedSecs = timeElapsedSecs;
        this.timeElapsedDesc = timeElapsedDesc;
    }

    public static CalculationResult create(PrimeCalculator calculator, BigInteger ceiling, int countOfPrimes, Integer keepLast,
                                           List<BigInteger> primes, BigDecimal timeElapsedSecs, String timeElapsedDesc) {
        return new CalculationResult(calculator.getClass().getSimpleName(), ceiling, countOfPrimes, keepLast,
                primes, timeElapsedSecs, timeElapsedDesc);
    }

    @JsonProperty
    public String getCalculatorType() {
        return calculatorType;
    }

    @JsonProperty
    public BigInteger getCeiling() {
        return ceiling;
    }

    @JsonProperty
    public Integer getKeepLast() {
        return keepLast;
    }

    @JsonProperty
    public int getCountOfPrimes() {
        return countOfPrimes;
    }

    @JsonProperty
    public BigDecimal getTimeElapsedSecs() {
        return timeElapsedSecs;
    }

    @JsonProperty
    public String getTimeElapsedDesc() {
        return timeElapsedDesc;
    }

    @VisibleForTesting
    public void setTimeElapsedSecs(BigDecimal timeElapsedSecs) {
        this.timeElapsedSecs = timeElapsedSecs;
    }

    @VisibleForTesting
    public void setTimeElapsedDesc(String timeElapsedDesc) {
        this.timeElapsedDesc = timeElapsedDesc;
    }

    @JsonProperty
    public List<BigInteger> getPrimes() {
        return primes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CalculationResult that = (CalculationResult) o;

        return new EqualsBuilder()
                .append(countOfPrimes, that.countOfPrimes)
                .append(calculatorType, that.calculatorType)
                .append(ceiling, that.ceiling)
                .append(timeElapsedDesc, that.timeElapsedDesc)
                .append(primes, that.primes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(calculatorType)
                .append(ceiling)
                .append(countOfPrimes)
                .append(timeElapsedDesc)
                .append(primes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
