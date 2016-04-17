package com.villarsolutions.primordial;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import com.villarsolutions.primordial.calculator.impl.BasicTrialDivision;
import com.villarsolutions.primordial.calculator.impl.EratosthenesSieve;
import com.villarsolutions.primordial.calculator.impl.ParallelSegmentedEratosthenesSieve;

/**
 * Dependency Injection class (google/guice) used to bind beans.
 */
class PrimordialModule extends AbstractModule {
    @Override
    protected void configure() {
        bindCalculatorBean(BasicTrialDivision.class);
        bindCalculatorBean(EratosthenesSieve.class);
        bindCalculatorBean(ParallelSegmentedEratosthenesSieve.class);
    }

    private <T extends PrimeCalculator> void bindCalculatorBean(Class<T> clazz) {
        bind(PrimeCalculator.class)
                .annotatedWith(Names.named(clazz.getSimpleName()))
                .to(clazz);
    }
}
