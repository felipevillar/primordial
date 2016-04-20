package com.villarsolutions.primordial.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;

public class PerformanceResultSummary {

    private long ceiling;
    private int countOfPrimes;

    private List<PerformanceResult> results = Lists.newArrayList();

    public PerformanceResultSummary() {
        // Jackson deserialization
    }

    public PerformanceResultSummary(long ceiling, int countOfPrimes, List<PerformanceResult> results) {
        this.ceiling = ceiling;
        this.countOfPrimes = countOfPrimes;
        this.results = results;
    }

    @JsonProperty
    public long getCeiling() {
        return ceiling;
    }

    @JsonProperty
    public int getCountOfPrimes() {
        return countOfPrimes;
    }

    @JsonProperty
    public List<PerformanceResult> getResults() {
        return results;
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
