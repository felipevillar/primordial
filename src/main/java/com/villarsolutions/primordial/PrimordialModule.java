package com.villarsolutions.primordial;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import com.villarsolutions.primordial.calculator.impl.BasicTrialDivision;
import com.villarsolutions.primordial.calculator.impl.EratosthenesSieve;
import com.villarsolutions.primordial.calculator.impl.ParallelEratosthenesSieve;
import com.villarsolutions.primordial.calculator.impl.aws.AWSLambdaCalculator;

/**
 * Dependency Injection class (google/guice) used to bind beans.
 */
class PrimordialModule extends AbstractModule {

    private final PrimordialConfiguration config;

    public PrimordialModule(PrimordialConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        bindCalculatorBean(BasicTrialDivision.class);
        bindCalculatorBean(EratosthenesSieve.class);
        bindCalculatorBeanInstance(new ParallelEratosthenesSieve(
            config.getLocalMinSegmentSize(),
            config.getLocalMaxSegmentSize(),
            config.getParallelismLowerBound()
        ));
        bindCalculatorBeanInstance(new AWSLambdaCalculator(
            config.getAwsRegion(),
            config.getAwsMinSegmentSize(),
            config.getAwsMaxSegmentSize(),
            config.getAwsLevelOfParallelism(),
            config.getParallelismLowerBound()
        ));
    }

    private <T extends PrimeCalculator> void bindCalculatorBean(Class<T> clazz) {
        bind(PrimeCalculator.class)
                .annotatedWith(Names.named(clazz.getSimpleName()))
                .to(clazz);
    }

    private void bindCalculatorBeanInstance(PrimeCalculator instance) {
        bind(PrimeCalculator.class)
            .annotatedWith(Names.named(instance.getClass().getSimpleName()))
            .toInstance(instance);
    }

}
