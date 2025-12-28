package cpe.simulator.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cpe.simulator.model.IncidentCreateRequest;
import cpe.simulator.model.IncidentCreateResponse;
import cpe.simulator.model.IncidentPhaseCreateRequest;
import cpe.simulator.model.PhaseType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IncidentApiClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String bearerToken;
    private final ObjectMapper mapper;

    public IncidentApiClient(String baseUrl, String bearerToken) {
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public List<PhaseType> getPhaseTypes()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/incidents/phase/types"))
                .header("Authorization", "Bearer " + bearerToken)
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("GET /incidents/phase/types failed");
        }

        return mapper.readValue(
            response.body(),
            new TypeReference<List<PhaseType>>() {}
        );
    }

        public Set<String> getValidIncidentCodes()
            throws IOException, InterruptedException {

        return getPhaseTypes().stream()
            .map(PhaseType::getCode)
            .collect(Collectors.toSet());
        }

        public IncidentCreateResponse createIncident(IncidentCreateRequest incident)
            throws IOException, InterruptedException {

        String body = mapper.writeValueAsString(incident);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/incidents"))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("POST /incidents failed");
        }

        return mapper.readValue(response.body(), IncidentCreateResponse.class);
    }

    public void createIncidentPhase(IncidentPhaseCreateRequest phase)
            throws IOException, InterruptedException {

        String body = mapper.writeValueAsString(phase);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/incident/phase"))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("POST /incident/phase failed");
        }
    }
}
