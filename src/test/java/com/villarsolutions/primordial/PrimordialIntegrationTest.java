package com.villarsolutions.primordial;

import com.google.common.collect.Lists;
import com.villarsolutions.primordial.api.CalculationResult;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

import static com.villarsolutions.primordial.PrimesResource.URL_BASE_PATH;
import static com.villarsolutions.primordial.PrimordialFixtures.createResultFromJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class PrimordialIntegrationTest {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-primordial.yml");

    @ClassRule
    public static final DropwizardAppRule<PrimordialConfiguration> RULE = new DropwizardAppRule<>(
            PrimordialApplication.class, CONFIG_PATH);

    private Client client;

    @Before
    public void setUp() throws Exception {
        client = ClientBuilder.newClient();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void whenUsingDefaultURL_thenASuccessfulResultIsReturned() throws Exception {
        final Response response = sendRequest(PrimordialFixtures.CEILING);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        CalculationResult result = response.readEntity(CalculationResult.class);
        CalculationResult expected = createResultFromJson();

        // Set the elapsed time to the value from our fixture, so that this
        // test passes despite variances of integration test elapsed time.
        result.setTimeElapsedSecs(BigDecimal.valueOf(0.001).setScale(3, BigDecimal.ROUND_DOWN));
        result.setTimeElapsedDesc("1.519 ms");

        assertEquals(expected, result);
    }

    @Test
    public void whenUsingInvalidCalculatorType_thenABadRequestResultIsReturned() throws Exception {
        final Response response = sendRequest("invalidCalculator", PrimordialFixtures.CEILING);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        String errorText = response.readEntity(String.class);
        assertThat(errorText).contains("There is no configured calculator with name [invalidCalculator]");
    }

    @Test
    public void whenTheCeilingIsLessThanTwo_thenABadRequestResultIsReturned() throws Exception {
        List<Response> responses = Lists.newArrayList();
        responses.add(sendRequest(1));
        responses.add(sendRequest(0));
        responses.add(sendRequest(-1));

        responses.forEach(r -> {
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
            String errorText = r.readEntity(String.class);
            assertThat(errorText).contains("The 'ceiling' must be greater than 1");
        });
    }

    @Test
    public void whenTheCeilingIsOutOfRange_thenAnInternalErrorResultIsReturned() throws Exception {
        Response response = sendRequest(Long.MAX_VALUE);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        String errorText = response.readEntity(String.class);
        assertThat(errorText).contains("This calculator only supports prime numbers up to [");
    }

    private Response sendRequest(long ceiling) {
        return sendRequest(null, ceiling);
    }

    private Response sendRequest(String calculatorType, long ceiling) {
        WebTarget webTarget = client.target("http://localhost:" + RULE.getLocalPort() + URL_BASE_PATH)
                .queryParam(PrimesResource.CEILING_PARAMETER, ceiling);

        if (calculatorType != null) {
            webTarget = webTarget.queryParam(PrimesResource.CALCULATOR_TYPE_PARAMETER, calculatorType);
        }

        return webTarget
                .request()
                .get(Response.class);
    }

}
