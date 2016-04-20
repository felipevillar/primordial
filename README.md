# Summary

This is an example of a Prime Number generator, exposed as a RESTful service.

It uses Dropwizard as the container, which includes Jetty for HTTP, Jersey for REST, Jackson for
JSON, as well as a built-in metrics library.

The service is able to generate prime numbers up to a specified 'ceiling', and includes three different
implementations for comparison.  The implementations can be queried independently without having to rebuild
the service, by using the 'calculatorType' parameter (more on this below).

The "ParallelSegmentedEratosthenesSieve" implementation is an example using Java 8 streams to
multi-thread a prime number generator.  After calculating the "small" prime numbers using a single-threaded
"Sieve of Eratosthenes" algorithm, it divides the rest of the number-line into "segments" and uses all
available cores on the machine to "sieve" these segments in parallel.   This results in much faster results,
e.g. 1 minute instead of 4 minutes to search for all prime numbers up to 2 billion.

# Building and Running

This service has been tested with Java 1.8.0_66 and Apache Maven 3.3.9

After cloning the project from GitHub into a local directory, run:

        mvn package

To run the server with 4GB heap, run:

        java -Xmx4G -jar target/primordial-1.0-SNAPSHOT.jar server primordial.yml

Note we have been able to generate primes up to 2 billion using the ParallelSegmentedEratosthenesSieve implementation
and 6GB of heap.  A heap of 2GB or 4GB is fine for smaller ranges.

To generate all primes up to 30 using the default calculator, run:

        http://localhost:8080/primes?ceiling=30

To generate all primes up to 1000 using specific calculators, run:

        http://localhost:8080/primes?calculatorType=EratosthenesSieve&ceiling=1000
        http://localhost:8080/primes?calculatorType=ParallelSegmentedEratosthenesSieve&ceiling=1000
        http://localhost:8080/primes?calculatorType=BasicTrialDivision&ceiling=1000

Note that the three available calculators at the moment are:

* `EratosthenesSieve`
* `ParallelSegmentedEratosthenesSieve`
* `BasicTrialDivision`

Refer to the JavaDoc in each of these classes for a description of each algorithm.

To keep only the last N prime numbers in the result (to avoid the browser hanging when searching
for large primes), you can use the `keepLast` parameter:

        http://localhost:8080/primes?calculatorType=ParallelSegmentedEratosthenesSieve&ceiling=100000&keepLast=5

To easily compare the performance of the three algorithms, you can run:

    http://localhost:8080/primes/performance?ceiling=100000

You can paste the JSON into this [JSON Viewer](http://jsonviewer.stack.hu/) for ease of use.

Note that the BasicTrialDivision algorithm is extremely slow.  For this reason it is not recommended to use
the `/performance` URL for ceiling values > 1,000,000.  You can however, try the other two algorithms with
much larger values, and the JSON response for the /primes path will include the time spent in the calculation.

If you start the server with 6GB, you should be able to run this query in approximately 26 seconds
(tested on an 8-core Intel Xeon CPU, 3.2 GHz)

        http://localhost:8080/primes?calculatorType=ParallelSegmentedEratosthenesSieve&ceiling=1000000000&keepLast=1

To stop the server gracefully press `ctrl-C`

# Testing

Included with the unit tests is an integration test (`PrimordialIntegrationTest.java`) which starts the server
on localhost:0, and sends requests to the process.

# JMX Metrics

The server will publish metrics as MBeans.  To view these, one can use JVisualVM (installing the MBean plugin)
then view the MBean `metrics/com.villarsolutions.primordial.PrimesResource.calculatePrime`

Statistics in this MBean include percentiles duration of the requests, count, max, mean, etc.

Note that the performance of the algorithms will vary if running multiple requests concurrently.


