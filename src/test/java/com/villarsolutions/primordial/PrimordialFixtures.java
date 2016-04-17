package com.villarsolutions.primordial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.villarsolutions.primordial.api.CalculationResult;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import io.dropwizard.jackson.Jackson;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static com.villarsolutions.primordial.util.PrimordialUtil.toBigInts;
import static io.dropwizard.testing.FixtureHelpers.fixture;

public class PrimordialFixtures {

    /**
     * Note that the CEILING parameter is inclusive.
     */
    public static final BigInteger CEILING = BigInteger.valueOf(997);

    public static final List<BigInteger> PRIMES_UP_TO_CEILING = toBigInts(Lists.newArrayList(2,3,5,7,11,13,17,19,23,29,31,
            37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,
            179,181,191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,
            331,337,347,349,353,359,367,373,379,383,389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,
            487,491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,607,613,617,619,631,641,643,647,
            653,659,661,673,677,683,691,701,709,719,727,733,739,743,751,757,761,769,773,787,797,809,811,821,823,827,
            829,839,853,857,859,863,877,881,883,887,907,911,919,929,937,941,947,953,967,971,977,983,991,997));

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final String FIXTURE_JSON = "fixtures/PrimesCalculationResult.json";

    public static CalculationResult createResultFromJson() {
        try {
            return MAPPER.readValue(fixture(FIXTURE_JSON), CalculationResult.class);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static <T extends PrimeCalculator> CalculationResult createResult(Class<T> calculatorClass) {
        try {
            return CalculationResult.create(calculatorClass.newInstance(), CEILING, 168, null, PRIMES_UP_TO_CEILING,
                    new BigDecimal(0.001).setScale(3, BigDecimal.ROUND_DOWN), "1.519 ms");
        } catch (InstantiationException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

}
