package com.villarsolutions.primordial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.metrics.graphite.GraphiteReporterFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

/**
 * DropWizard configuration class.  Initialized with the properties
 * defined in the yml file used to start the application.
 */
public class PrimordialConfiguration extends Configuration {

    @NotEmpty
    private String defaultCalculator;

    @Valid
    private GraphiteReporterFactory graphiteReporterFactory = new GraphiteReporterFactory();

    public String getDefaultCalculator() {
        return defaultCalculator;
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
