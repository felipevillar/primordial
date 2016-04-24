# Summary

This is an example of a Prime Number generator, exposed as a RESTful service.

It is also an example of multi-threading with Java 8 and of using AWS Lambda with the AWS Java SDK to do parallel and "serverless" computation in the cloud.

It uses Dropwizard as the container, which includes Jetty for HTTP, Jersey for REST, Jackson for JSON, as well as a built-in metrics library.

The service is able to generate prime numbers up to a specified 'ceiling', and includes four different implementations for comparison.  The implementations can be queried independently without having to rebuild the service, by using the `calculatorType` parameter (more on this below).

The `ParallelEratosthenesSieve` implementation uses executors and Java 8 streams to multi-thread the work of generating prime numbers.  After calculating the "small" prime numbers using a single-threaded "Sieve of Eratosthenes" algorithm, it divides the rest of the number-line into "segments" and uses all available cores on the machine to "sieve" these segments in parallel. This yields much faster results, (under 1 minute instead of 4 minutes to search for all prime numbers up to 2 billion - on an 8-core Intel Xeon CPU, 3.2 GHz)

The `AWSLambdaCalculator` implementation shows how to implement "serverless" computing by farming off work to the AWS compute grid.

# Building and Running

This service has been tested with Java 1.8.0_66 and Apache Maven 3.3.9

After cloning the project from GitHub into a local directory, run:

        mvn package

Optionally, to install the code to AWS Lambda, you can run:

        mvn install

To run the server with 6GB heap, run:

        java -Xmx6G -jar target/primordial-1.0-SNAPSHOT.jar server primordial.yml

We have been able to generate primes up to 2 billion using the ParallelEratosthenesSieve implementation and 6GB of heap.  A heap of 2GB or 4GB is fine for smaller ranges.

To generate all primes up to 30 using the default calculator, run:

        http://localhost:8080/primes?ceiling=30

To generate all primes up to 1000 using specific calculators, run:

        http://localhost:8080/primes?calculatorType=EratosthenesSieve&ceiling=1000
        http://localhost:8080/primes?calculatorType=ParallelEratosthenesSieve&ceiling=1000
        http://localhost:8080/primes?calculatorType=AWSLambdaCalculator&ceiling=1000
        http://localhost:8080/primes?calculatorType=BasicTrialDivision&ceiling=1000

Note that the four available calculators at the moment are:

  * `EratosthenesSieve`
  * `ParallelEratosthenesSieve`
  * `AWSLambdaCalculator` 
  * `BasicTrialDivision`

Refer to the JavaDoc in each of these classes for a description of each algorithm.

To keep only the last N prime numbers in the result (to avoid the browser hanging when searching for large primes), you can use the `keepLast` parameter:

        http://localhost:8080/primes?calculatorType=ParallelEratosthenesSieve&ceiling=100000&keepLast=5

To easily compare the performance of the four algorithms, you can run:

    http://localhost:8080/primes/performance?ceiling=100000

You can paste the JSON into this [JSON Viewer](http://jsonviewer.stack.hu/) for ease of use.

Note that the BasicTrialDivision algorithm is extremely slow.  For this reason it is not recommended to use the `/performance` URL for ceiling values higher than 5 or 10 million.  You can however, try the other three algorithms with much larger values, using the calculatorType parameter.  The JSON response always includes the time spent in the calculation.

If you start the server with 6GB, you should be able to run this query in approximately 20 to 25 seconds (tested on an 8-core Intel Xeon CPU, 3.2 GHz)

        http://localhost:8080/primes?calculatorType=ParallelEratosthenesSieve&ceiling=1000000000&keepLast=1

To stop the server gracefully press `ctrl-C`

# Testing

Included with the unit tests is an integration test (`PrimordialIntegrationTest.java`) which starts the server on localhost:0, and sends requests to the process.

# JMX Metrics

The server will publish metrics as MBeans.  To view these, one can use JVisualVM (installing the MBean plugin) then view the MBean `metrics/com.villarsolutions.primordial.PrimesResource.calculatePrime`

Statistics in this MBean include percentiles duration of the requests, count, max, mean, etc.

Note that the performance of the algorithms will vary if running multiple requests concurrently.

# AWS Lambda

This project's Maven `pom.xml` includes an execution bound to the `install` lifecycle phase that will automatically deploy the Lambda function to the AWS servers via Amazon S3.  If you are not familiar with AWS Lambda you may refer to the [online documentation](http://docs.aws.amazon.com/lambda/latest/dg/welcome.html) 

For this to work, the following is required:

  * A valid AWS Lambda account. [AWS Account Setup Instructions](http://docs.aws.amazon.com/lambda/latest/dg/setup.html)
  * A valid credentials file: `~/.aws/credentials` (in Windows: `C:\Users\${user.name}\.aws\credentials`)  
  * Specify the following properties in `aws.properties`
    * `aws.region`
    * `aws.role.arn`
    * `aws.s3.bucket`

Once you have an AWS Lambda account, you can create credentials files, create users and manage roles by using the [IAM Management Console](https://console.aws.amazon.com/iam/home)

In order to deploy the lambda your user must have access to S3.  You can grant the `AmazonS3FullAccess` privilege via the IAM Management Console.

You can see metrics and monitor the execution of lambdas via the [AWS Management Console](https://us-west-2.console.aws.amazon.com/console/home)

To see the logs of the lambda executions, you can use [Amazon CloudWatch](https://eu-west-1.console.aws.amazon.com/cloudwatch/home)

Other useful links:

  * [AWS SDK for Java API Reference](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html)
  * [Programming Model for Authoring Lambda Functions in Java](http://docs.aws.amazon.com/lambda/latest/dg/java-programming-model.html)
    
    