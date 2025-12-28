package cpe.simulator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cpe.simulator.api.IncidentApiClient;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SimulatorAppTest {

    @Test
    void sendsIncidentWhenCodeValid() throws Exception {
    FakeClient api = new FakeClient(Set.of("FIRE"));

    String output = runAndCaptureOutput(() ->
        SimulatorApp.handleIncidentCode("FIRE", () -> api)
    );

    assertTrue(api.posted);
    assertTrue("FIRE".equals(api.lastIncident.getCode()));
        assertTrue(output.contains("Incident envoyé : FIRE"));
    }

    @Test
    void skipsIncidentWhenCodeInvalid() throws Exception {
    FakeClient api = new FakeClient(Set.of("OTHER"));

    String output = runAndCaptureOutput(() ->
        SimulatorApp.handleIncidentCode("FIRE", () -> api)
    );

        assertFalse(api.posted);
        assertTrue(output.contains("Incident ignoré (code invalide) : FIRE"));
    }

    @Test
    void skipsIncidentWhenCodeIsNeutral000() throws Exception {
        String output = runAndCaptureOutput(() ->
                SimulatorApp.handleIncidentCode("000", () -> {
                    throw new AssertionError("API client should not be constructed for neutral code");
                })
        );

        assertTrue(output.contains("Incident neutre ignoré : 000"));
    }

    private String runAndCaptureOutput(ThrowingRunnable action) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOut);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static class FakeClient extends IncidentApiClient {
        private final Set<String> valid;
        boolean posted = false;
        cpe.simulator.model.Incident lastIncident;

        FakeClient(Set<String> valid) {
            super("http://localhost", "token");
            this.valid = valid;
        }

        @Override
        public Set<String> getValidIncidentCodes() {
            return valid;
        }

        @Override
        public void postIncident(cpe.simulator.model.Incident incident) {
            posted = true;
            lastIncident = incident;
        }
    }
}
