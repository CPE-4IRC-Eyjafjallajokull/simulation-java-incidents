package cpe.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import cpe.simulator.api.IncidentApiClient;
import cpe.simulator.loader.IncidentProbabilityLoader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class SimulatorAppTest {

    @Test
    void sendsIncidentWhenCodeValid() throws Exception {
        Map<String, Double> probabilities = Map.of("FIRE", 1.0);

        try (MockedStatic<IncidentProbabilityLoader> loaderMock = mockStatic(IncidentProbabilityLoader.class);
             MockedConstruction<IncidentApiClient> apiConstruction = mockConstruction(IncidentApiClient.class, (mock, context) ->
                     org.mockito.Mockito.when(mock.getValidIncidentCodes()).thenReturn(Set.of("FIRE")))) {

            loaderMock.when(() -> IncidentProbabilityLoader.load("incident-probabilities.json"))
                      .thenReturn(probabilities);

            String output = runMainAndCaptureOutput();

            assertEquals(1, apiConstruction.constructed().size(), "An API client should be constructed");
            IncidentApiClient apiMock = apiConstruction.constructed().get(0);

            verify(apiMock).postIncident(argThat(incident -> "FIRE".equals(incident.getCode())));
            assertTrue(output.contains("Incident envoyé : FIRE"));
        }
    }

    @Test
    void skipsIncidentWhenCodeInvalid() throws Exception {
        Map<String, Double> probabilities = Map.of("FIRE", 1.0);

        try (MockedStatic<IncidentProbabilityLoader> loaderMock = mockStatic(IncidentProbabilityLoader.class);
             MockedConstruction<IncidentApiClient> apiConstruction = mockConstruction(IncidentApiClient.class, (mock, context) ->
                     org.mockito.Mockito.when(mock.getValidIncidentCodes()).thenReturn(Set.of("OTHER")))) {

            loaderMock.when(() -> IncidentProbabilityLoader.load("incident-probabilities.json"))
                      .thenReturn(probabilities);

            String output = runMainAndCaptureOutput();

            assertEquals(1, apiConstruction.constructed().size(), "An API client should be constructed");
            IncidentApiClient apiMock = apiConstruction.constructed().get(0);

            verify(apiMock, never()).postIncident(any());
            assertTrue(output.contains("Incident ignoré (code invalide) : FIRE"));
        }
    }

    private String runMainAndCaptureOutput() throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            SimulatorApp.main(new String[0]);
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOut);
        }
    }
}
