package com.villarsolutions.primordial;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.villarsolutions.primordial.api.CalculationResult;
import com.villarsolutions.primordial.api.PerformanceResult;
import com.villarsolutions.primordial.api.PerformanceResultSummary;
import com.villarsolutions.primordial.calculator.PrimeCalculator;
import com.villarsolutions.primordial.exception.CalculationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.villarsolutions.primordial.util.PrimordialUtil.getDecimalFormat;

/**
 * This is the main DropWizard Resource class for the Primordial application.
 * <p>
 * DropWizard resources are similar to Spring controllers, in that they map URL paths to application code.
 * <p>
 * This class supports two paths:
 * <ul>
 *     <li>/primes - used to calculate prime numbers</li>
 *     <li>/primes/performance - used to run all configured calculators sequentially and collate the run-times into a single response</li>
 * </ul>
 * Both paths accept a <code>ceiling</code> parameter, which can be used to specify the upper limit for the prime number search.
 * <p>
 * The /primes path also accepts a <code>calculatorType</code> parameter, which can be used to specify the calculator implementation to use.
 * <p>
 * Finally, the /primes path can be given a <code>keepLast</code> parameter which can be used to limit the returned prime numbers to the
 * "last N prime numbers" found.
 */
@Path(PrimesResource.URL_BASE_PATH)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class PrimesResource {

    private static final Logger log = LoggerFactory.getLogger(PrimesResource.class);

    public static final String URL_BASE_PATH = "/primes";
    public static final String CEILING_PARAMETER = "ceiling";
    public static final String CALCULATOR_TYPE_PARAMETER = "calculatorType";
    public static final String KEEP_LAST_PARAMETER = "keepLast";

    private final Injector injector;
    private final PrimeCalculator defaultCalculator;

    public PrimesResource(Injector injector, String defaultCalculator) {
        this.injector = injector;
        this.defaultCalculator = getCalculatorBean(defaultCalculator).orElseThrow(() -> new IllegalArgumentException(noCalculatorMessage(defaultCalculator)));
        log.info(String.format("%s has been successfully initialized with default calculator [%s]", getClass().getSimpleName(), defaultCalculator));
    }

    @GET
    @Timed
    public Response calculatePrime(@QueryParam(CALCULATOR_TYPE_PARAMETER) String calculatorType, @QueryParam(CEILING_PARAMETER) Long ceiling, @QueryParam(KEEP_LAST_PARAMETER) Integer keepLast) {
        checkParameter(CEILING_PARAMETER, ceiling);
        PrimeCalculator calculator = calculatorType == null ? defaultCalculator : getCalculatorBean(calculatorType).orElseThrow(() -> new BadRequestException(noCalculatorMessage(calculatorType)));
        return calculatePrime(calculator, ceiling, keepLast);
    }

    @GET
    @Path("/performance")
    @Timed
    public Response performanceRun(@QueryParam(CEILING_PARAMETER) Long ceiling) {
        checkParameter(CEILING_PARAMETER, ceiling);
        validateCeiling(ceiling);

        List<Binding<PrimeCalculator>> calculatorBindings = injector.findBindingsByType(TypeLiteral.get(PrimeCalculator.class));
        try {
            List<CalculationResult> calculationResults = calculatorBindings.stream()
                    .map(c -> getCalculationResult(c.getProvider().get(), ceiling, null))
                    .collect(Collectors.toList());

            int countOfPrimes = 0;
            List<PerformanceResult> results = Lists.newArrayList();
            if (!calculationResults.isEmpty()) {
                countOfPrimes = calculationResults.get(0).getCountOfPrimes();
                calculationResults.stream()
                        .map(result -> new PerformanceResult(result.getCalculatorType(), result.getTimeElapsedSecs(), result.getTimeElapsedDesc()))
                        .sorted()
                        .forEach(results::add);
            }

            PerformanceResultSummary summary = new PerformanceResultSummary(ceiling, countOfPrimes, results);
            return Response.ok(summary).build();
        } catch (CalculationException e) {
            String msg = String.format("Could not complete performance run for ceiling [%s] run due to CalculationException", getDecimalFormat().format(ceiling));
            log.error(msg, e);
            return  Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Response calculatePrime(PrimeCalculator calculator, long ceiling, Integer keepLast) {
        validateCeiling(ceiling);
        validateKeepLast(keepLast);

        try {
            CalculationResult result = getCalculationResult(calculator, ceiling, keepLast);
            return Response.ok(result).build();
        } catch (CalculationException e) {
            String msg = String.format("Calculator [%s] threw an error when computing primes up to ceiling [%s]", calculator.getClass().getSimpleName(), getDecimalFormat().format(ceiling));
            log.error(msg, e);
            return  Response.serverError().entity(e.getMessage()).build();
        }
    }

    private CalculationResult getCalculationResult(PrimeCalculator calculator, long ceiling, Integer keepLast) throws CalculationException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Long> primes = calculator.calculatePrimes(ceiling);

        int countOfPrimes = primes.size();

        // If the keepLast parameter is specified, only keep the last N primes from the result.
        if (keepLast != null) {
            primes = primes.stream()
                    .skip(Math.max(primes.size() - keepLast, 0))
                    .collect(Collectors.toList());
        }

        stopwatch.stop();
        double elapsedMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        BigDecimal elapsedSecs = BigDecimal.valueOf(elapsedMillis / 1000).setScale(3, BigDecimal.ROUND_DOWN);

        return CalculationResult.create(calculator, ceiling, countOfPrimes, keepLast, primes, elapsedSecs, stopwatch.toString());
    }

    private Optional<PrimeCalculator> getCalculatorBean(String calculatorType) {
        try {
            return Optional.of(injector.getInstance(Key.get(PrimeCalculator.class, Names.named(calculatorType))));
        } catch (ConfigurationException e) {
            log.warn(String.format("Invalid attempt to retrieve a calculatorType with name [%s]", calculatorType));
            return Optional.empty();
        }
    }

    private static String noCalculatorMessage(String defaultCalculator) {
        return String.format("There is no configured calculator with name [%s]", defaultCalculator);
    }

    private static void validateCeiling(long ceiling) {
        if (ceiling <= 1) {
            throw new BadRequestException("The 'ceiling' must be greater than 1");
        }
    }

    /**
     * <code>keepLast</code> is an optional parameter, but if specified it must be a number
     * greater than 0
     */
    private void validateKeepLast(Integer keepLast) {
        if (keepLast != null) {
            if (keepLast <= 0) {
                throw new BadRequestException("The 'keepLast' parameter must be greater than 0");
            }
        }
    }

    private static <T> void checkParameter(String name, T value) {
        if (value == null) {
            String msg = String.format("The required parameter [%s] was not specified.", name);
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

}
