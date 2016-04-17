package com.villarsolutions.primordial.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;

public class PerformanceResult implements Comparable<PerformanceResult> {

    private String calculatorType;
    private BigDecimal timeElapsedSecs;
    private String timeElapsedDesc;

    public PerformanceResult() {
        // Jackson deserialization
    }

    public PerformanceResult(String calculatorType, BigDecimal timeElapsedSecs, String timeElapsedDesc) {
        this.calculatorType = calculatorType;
        this.timeElapsedSecs = timeElapsedSecs;
        this.timeElapsedDesc = timeElapsedDesc;
    }

    @Override
    public int compareTo(PerformanceResult o) {
        return getTimeElapsedSecs().compareTo(o.getTimeElapsedSecs());
    }

    @JsonProperty
    public String getCalculatorType() {
        return calculatorType;
    }

    @JsonProperty
    public BigDecimal getTimeElapsedSecs() {
        return timeElapsedSecs;
    }

    @JsonProperty
    public String getTimeElapsedDesc() {
        return timeElapsedDesc;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
