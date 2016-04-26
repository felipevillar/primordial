package com.villarsolutions.primordial.calculator.impl.aws;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.google.common.base.Stopwatch;
import com.villarsolutions.primordial.calculator.impl.AbstractSegmentedSieveCalculator;
import com.villarsolutions.primordial.calculator.impl.ParallelEratosthenesSieve;
import com.villarsolutions.primordial.calculator.impl.Segment;
import com.villarsolutions.primordial.exception.CalculationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Prime number calculator that uses a segmented Sieve of Eratosthenes algorithm and
 * works on each segment in parallel using AWS Lambdas.
 * <p>
 * It uses a fixed thread-pool to create N threads (where N = <code>levelOfParallelism</code>)
 * <p>
 * Each thread then computes the primes in each segment by invoking an AWS Lambda
 * <p>
 * For this calculator to work, the lambda must first be deployed to AWS using the
 * <code>mvn install</code> Maven lifecycle phase.
 * <p>
 * The properties in the <code>aws.properties</code> file must be correctly set, and a AWS
 * credentials file must exist in <code>~/.aws/credentials</code>
 * (in Windows: <code>C:\Users\${user.name}\.aws\credentials</code>)
 * <p>
 * For more information, refer to the README.md file in the AWS Lambda section.
 */
public class AWSLambdaCalculator extends AbstractSegmentedSieveCalculator {

    private static final Logger log = LoggerFactory.getLogger(AWSLambdaCalculator.class);

    private final Regions awsRegion;

    public AWSLambdaCalculator(Regions awsRegion, int minSegmentSize, int maxSegmentSize, int levelOfParallelism, int parallelismLowerBound) {
        super(minSegmentSize, maxSegmentSize, levelOfParallelism, parallelismLowerBound);
        this.awsRegion = awsRegion;
    }

    @Override
    protected List<Long> calculate(long ceiling) throws CalculationException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Segment> segments = getSegments(ceiling);
        List<Integer> smallPrimes = findPrimesInFirstSegment(stopwatch, segments);

        ExecutorService executorService = Executors.newFixedThreadPool(getLevelOfParallelism());
        List<Future<List<Long>>> futures = segments.stream()
            .skip(1)
            .map(segment -> executorService.submit(() -> executeLambda(smallPrimes, segment)))
            .collect(Collectors.toList());

        List<Long> result = concatenate(smallPrimes, futures);
        log.info(String.format("Calculation completed. Found [%d] primes overall. Time elapsed = %s", result.size(), stopwatch));
        return result;
    }

    private LambdaPrimeSieve createLambda() {
        AWSLambdaClient lambdaClient = new AWSLambdaClient();
        lambdaClient.configureRegion(awsRegion);
        return LambdaInvokerFactory.build(LambdaPrimeSieve.class, lambdaClient);
    }

    private List<Long> executeLambda(List<Integer> smallPrimes, Segment segment) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LambdaPrimeSieve lambda = createLambda();
        log.info(String.format("Created Lambda in [%s] for %s", stopwatch, segment));
        List<Long> primesInSegment = lambda.calculatePrimesInSegment(SieveSegmentRequest.create(smallPrimes, segment));
        log.info(String.format("Executed Lambda in [%s] and found [%d] primes for %s", stopwatch, primesInSegment.size(), segment));
        return primesInSegment;
    }

    /**
     * Same reason as for the limit in <code>ParallelEratosthenesSieve.getMaxCeilingSupported()</code>.
     *
     * @see ParallelEratosthenesSieve#getMaxCeilingSupported()
     */
    @Override
    protected Optional<Long> getMaxCeilingSupported() {
        return Optional.of((long) Math.pow(Integer.MAX_VALUE, 2));
    }
}
