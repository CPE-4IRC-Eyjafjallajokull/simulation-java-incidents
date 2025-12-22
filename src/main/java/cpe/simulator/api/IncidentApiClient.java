package cpe.simulator.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cpe.simulator.model.Incident;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

public class IncidentApiClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String bearerToken;

    public IncidentApiClient(String baseUrl, String bearerToken) {
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
    }

    public Set<String> getValidIncidentCodes()
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

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                response.body(),
                new TypeReference<Set<String>>() {}
        );
    }

    public void postIncident(Incident incident)
            throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(incident);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/incident"))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new RuntimeException("POST /incident failed");
        }
    }
}
