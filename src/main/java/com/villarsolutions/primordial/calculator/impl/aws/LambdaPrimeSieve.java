package com.villarsolutions.primordial.calculator.impl.aws;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaFunction;

import java.util.List;

/**
 * Interface used by the AWSLambdaClient in order to remotely
 * execute the lambda on AWS.
 *
 * @see AWSLambdaClient
 * @see AWSLambdaCalculator
 */
public interface LambdaPrimeSieve {

    @LambdaFunction(functionName = "LambdaPrimeSieve")
    List<Long> calculatePrimesInSegment(SieveSegmentRequest request);

}
