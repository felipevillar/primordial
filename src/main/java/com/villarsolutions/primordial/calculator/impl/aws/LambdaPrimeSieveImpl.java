package com.villarsolutions.primordial.calculator.impl.aws;

import com.villarsolutions.primordial.calculator.impl.Segment;
import com.villarsolutions.primordial.calculator.impl.SegmentedSieveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * Lambda implementation class with a method to return all the prime numbers in a
 * given <code>Segment</code>. This is executed inside AWS Lambda, when invoked by
 * AWSLambdaCalculator
 *
 * @see AWSLambdaCalculator
 */
@ThreadSafe
public class LambdaPrimeSieveImpl {

    private static final Logger log = LoggerFactory.getLogger(LambdaPrimeSieveImpl.class);

    public static List<Long> calculatePrimesInSegment(SieveSegmentRequest request) {
        log.info(String.format("About to process sieve request for segment: %s", request.getSegment()));
        List<Integer> smallPrimes = request.getSmallPrimes();
        Segment segment = request.getSegment();
        return SegmentedSieveUtil.calculatePrimesInSegment(smallPrimes, segment);
    }

}
