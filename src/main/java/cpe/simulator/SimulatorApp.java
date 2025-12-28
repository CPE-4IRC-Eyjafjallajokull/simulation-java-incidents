package cpe.simulator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import java.util.List;
import java.util.Random;

import cpe.simulator.api.IncidentApiClient;
import cpe.simulator.config.SimulatorConfig;
import cpe.simulator.loader.IncidentProbabilityLoader;
import cpe.simulator.loader.GeoZoneLoader;
import cpe.simulator.model.IncidentCreateRequest;
import cpe.simulator.model.IncidentCreateResponse;
import cpe.simulator.model.IncidentPhaseCreateRequest;
import cpe.simulator.model.PhaseType;
import cpe.simulator.model.GeoZone;
import cpe.simulator.rng.IncidentSelector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimulatorApp {

    public static void main(String[] args) throws Exception {

        Map<String, Double> probabilities =
                IncidentProbabilityLoader.load("incident-probabilities.json");

        IncidentSelector selector =
                new IncidentSelector(probabilities, SimulatorConfig.RNG_SEED);

        String incidentCode = selector.pickIncidentCode();

        handleIncidentCode(incidentCode, () ->
                new IncidentApiClient(
                        SimulatorConfig.API_BASE_URL,
                        SimulatorConfig.API_TOKEN
                )
        );
    }

    static void handleIncidentCode(String incidentCode,
                                   Supplier<IncidentApiClient> apiSupplier)
            throws Exception {

        if ("000".equals(incidentCode)) {
            System.out.println("ℹ️ Incident neutre ignoré : " + incidentCode);
            return;
        }

        IncidentApiClient api = apiSupplier.get();
        List<PhaseType> phaseTypes = api.getPhaseTypes();
        PhaseType phaseType = phaseTypes.stream()
                .filter(pt -> incidentCode.equals(pt.getCode()))
                .findFirst()
                .orElse(null);

        if (phaseType == null) {
            System.out.println("⚠️ Incident ignoré (code invalide) : " + incidentCode);
            return;
        }

        IncidentCreateRequest request = buildIncidentRequest(incidentCode);
        IncidentCreateResponse response = api.createIncident(request);

        IncidentPhaseCreateRequest phaseRequest = new IncidentPhaseCreateRequest();
        phaseRequest.setIncidentId(response.getIncidentId());
        phaseRequest.setPhaseTypeId(phaseType.getPhaseTypeId());
        phaseRequest.setPriority(phaseType.getDefaultCriticity());
        phaseRequest.setStartedAt(request.getEndedAt());
        phaseRequest.setEndedAt(null);

        api.createIncidentPhase(phaseRequest);
        System.out.println("✅ Incident envoyé : " + incidentCode + " (" + response.getIncidentId() + ")");
    }

    private static IncidentCreateRequest buildIncidentRequest(String incidentCode) throws Exception {
        IncidentCreateRequest request = new IncidentCreateRequest();
        request.setCreatedByOperatorId(System.getenv().getOrDefault("OPERATOR_ID", "operator-demo"));
        request.setAddress("Adresse simulée");
        request.setZipcode("00000");
        request.setCity("Ville");
        GeoZone zone = GeoZoneLoader.load(SimulatorConfig.GEO_ZONES_PATH, SimulatorConfig.GEO_ZONE_NAME);
        Random rng = new Random(SimulatorConfig.RNG_SEED + 1);
        request.setLatitude(randomInRange(rng, zone.getLatitudeMin(), zone.getLatitudeMax()));
        request.setLongitude(randomInRange(rng, zone.getLongitudeMin(), zone.getLongitudeMax()));
        request.setDescription("Incident " + incidentCode + " simulé");
        request.setEndedAt(null);

        if (Boolean.parseBoolean(System.getenv().getOrDefault("ENABLE_REVERSE_GEOCODE", "true"))) {
            enrichAddressFromReverse(request);
        }
        return request;
    }

    private static double randomInRange(Random rng, double min, double max) {
        return min + (max - min) * rng.nextDouble();
    }

    private static void enrichAddressFromReverse(IncidentCreateRequest request) {
        try {
            String url = String.format(
                    "%slat=%f&lon=%f",
                    System.getenv().getOrDefault("REVERSE_GEOCODE_URL", "https://api-adresse.data.gouv.fr/reverse/?"),
                    request.getLatitude(),
                    request.getLongitude()
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode features = root.get("features");
                if (features != null && features.isArray() && features.size() > 0) {
                    JsonNode props = features.get(0).get("properties");
                    if (props != null) {
                        if (props.hasNonNull("label")) {
                            request.setAddress(props.get("label").asText());
                        }
                        if (props.hasNonNull("postcode")) {
                            request.setZipcode(props.get("postcode").asText());
                        }
                        if (props.hasNonNull("city")) {
                            request.setCity(props.get("city").asText());
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Keep default simulated address if reverse geocoding fails
        }
    }
}
