package com.villarsolutions.primordial;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.villarsolutions.primordial.health.DefaultHealthCheck;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Main entry point for a DropWizard Application.
 * <p>
 * Running this class starts a RESTful service that listens on the ports
 * specified in the yml file that is passed as a parameter.
 */
public class PrimordialApplication extends Application<PrimordialConfiguration> {

    private static final String APPLICATION_NAME = "primordial";

    private Injector injector;

    public static void main(String[] args) throws Exception {
        new PrimordialApplication().run(args);
    }

    @Override
    public String getName() {
        return APPLICATION_NAME;
    }

    @Override
    public void initialize(Bootstrap<PrimordialConfiguration> bootstrap) {
        injector = Guice.createInjector(new PrimordialModule());
        bootstrap.addBundle(new AssetsBundle());

        final MetricRegistry metrics = new MetricRegistry();
        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();
    }


    @Override
    public void run(PrimordialConfiguration configuration, Environment environment) {
        environment.healthChecks().register("default", new DefaultHealthCheck());
        environment.jersey().register(new PrimesResource(injector, configuration.getDefaultCalculator()));
    }
}
