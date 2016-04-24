package com.villarsolutions.primordial;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.metrics.graphite.GraphiteReporterFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * DropWizard configuration class.  Initialized with the properties
 * defined in the yml file used to start the application.
 */
public class PrimordialConfiguration extends Configuration {

    @NotEmpty
    private String defaultCalculator;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int parallelismLowerBound;

    @Valid
    private Regions awsRegion;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int awsLevelOfParallelism;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int awsMinSegmentSize;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int awsMaxSegmentSize;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int localMinSegmentSize;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int localMaxSegmentSize;


    @Valid
    private GraphiteReporterFactory graphiteReporterFactory = new GraphiteReporterFactory();

    public String getDefaultCalculator() {
        return defaultCalculator;
    }

    public int getParallelismLowerBound() {
        return parallelismLowerBound;
    }

    public Regions getAwsRegion() {
        return awsRegion;
    }

    public int getAwsLevelOfParallelism() {
        return awsLevelOfParallelism;
    }

    public int getAwsMinSegmentSize() {
        return awsMinSegmentSize;
    }

    public int getAwsMaxSegmentSize() {
        return awsMaxSegmentSize;
    }

    public int getLocalMinSegmentSize() {
        return localMinSegmentSize;
    }

    public int getLocalMaxSegmentSize() {
        return localMaxSegmentSize;
    }

    @JsonProperty("metrics")
    public GraphiteReporterFactory getGraphiteReporterFactory() {
        return graphiteReporterFactory;
    }

    @JsonProperty("metrics")
    public void setGraphiteReporterFactory(GraphiteReporterFactory graphiteReporterFactory) {
        this.graphiteReporterFactory = graphiteReporterFactory;
    }
}
