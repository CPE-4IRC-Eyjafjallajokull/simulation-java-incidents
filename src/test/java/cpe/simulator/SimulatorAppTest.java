package cpe.simulator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cpe.simulator.api.IncidentApiClient;

import cpe.simulator.model.IncidentCreateRequest;
import cpe.simulator.model.IncidentCreateResponse;
import cpe.simulator.model.IncidentPhaseCreateRequest;
import cpe.simulator.model.PhaseType;
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
    assertTrue("FIRE".equals(api.lastPhaseTypeCode));
    assertWithinGeoBounds(api.lastLatitude, api.lastLongitude);
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

    private void assertWithinGeoBounds(double lat, double lon) {
        // Based on src/main/resources/geographic-zone.json default zone lyon_villeurbanne
        double latMin = 45.72;
        double latMax = 45.81;
        double lonMin = 4.80;
        double lonMax = 4.93;
        assertTrue(lat >= latMin && lat <= latMax, "Latitude out of bounds");
        assertTrue(lon >= lonMin && lon <= lonMax, "Longitude out of bounds");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static class FakeClient extends IncidentApiClient {
        private final Set<String> valid;
        boolean posted = false;
        String lastPhaseTypeCode;
        String lastIncidentId;
        double lastLatitude;
        double lastLongitude;

        FakeClient(Set<String> valid) {
            super("http://localhost", "token");
            this.valid = valid;
        }

        @Override
        public java.util.List<PhaseType> getPhaseTypes() {
            return valid.stream().map(code -> {
                PhaseType pt = new PhaseType();
                pt.setCode(code);
                pt.setPhaseTypeId(code + "-id");
                pt.setDefaultCriticity(0);
                return pt;
            }).toList();
        }

        @Override
        public IncidentCreateResponse createIncident(IncidentCreateRequest incident) {
            IncidentCreateResponse resp = new IncidentCreateResponse();
            resp.setIncidentId("incident-id");
            this.lastLatitude = incident.getLatitude();
            this.lastLongitude = incident.getLongitude();
            return resp;
        }

        @Override
        public void createIncidentPhase(IncidentPhaseCreateRequest phase) {
            posted = true;
            lastPhaseTypeCode = phase.getPhaseTypeId().replace("-id", "");
            lastIncidentId = phase.getIncidentId();
        }
    }
}
