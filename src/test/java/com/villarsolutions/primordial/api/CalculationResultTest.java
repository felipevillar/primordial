package com.villarsolutions.primordial.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villarsolutions.primordial.calculator.impl.EratosthenesSieve;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static com.villarsolutions.primordial.PrimordialFixtures.createResult;
import static com.villarsolutions.primordial.PrimordialFixtures.createResultFromJson;
import static org.junit.Assert.assertEquals;

public class CalculationResultTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws Exception {
        // Normalising the fixture into JSON string so that formatting does not affect the results.
        final String expected = MAPPER.writeValueAsString(createResultFromJson());
        final String actual = MAPPER.writeValueAsString(createResult(EratosthenesSieve.class));
        assertEquals(expected, actual);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        assertEquals(createResult(EratosthenesSieve.class), createResultFromJson());
    }


}