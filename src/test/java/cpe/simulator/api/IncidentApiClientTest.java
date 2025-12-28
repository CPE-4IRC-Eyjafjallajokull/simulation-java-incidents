package cpe.simulator.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cpe.simulator.model.IncidentCreateRequest;
import cpe.simulator.model.IncidentCreateResponse;
import cpe.simulator.model.IncidentPhaseCreateRequest;
import cpe.simulator.model.PhaseType;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class IncidentApiClientTest {

    @Test
    void getPhaseTypesReturnsServerValues() throws Exception {
        HttpServer server = startServer(exchange -> respondJson(exchange, 200, "[{\"code\":\"X\",\"phase_type_id\":\"id-x\"},{\"code\":\"Y\",\"phase_type_id\":\"id-y\"}]"));
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            List<PhaseType> types = client.getPhaseTypes();

            assertEquals("X", types.get(0).getCode());
            assertEquals("id-x", types.get(0).getPhaseTypeId());
            assertEquals("Y", types.get(1).getCode());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void createIncidentSendsBodyAndReturnsId() throws Exception {
        StringBuilder capturedBody = new StringBuilder();
        HttpServer server = startServer(exchange -> {
            capturedBody.append(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respondJson(exchange, 201, "{\"incident_id\":\"id-123\"}");
        });
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            IncidentCreateRequest req = new IncidentCreateRequest();
            req.setCreatedByOperatorId("op");
            req.setEndedAt(LocalDateTime.now());
            IncidentCreateResponse resp = client.createIncident(req);

            assertEquals(true, capturedBody.toString().contains("op"));
            assertEquals("id-123", resp.getIncidentId());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void createIncidentPhasePostsPayload() throws Exception {
        StringBuilder capturedBody = new StringBuilder();
        HttpServer server = startServer(exchange -> {
            capturedBody.append(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respondJson(exchange, 201, "{}");
        });
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            IncidentPhaseCreateRequest req = new IncidentPhaseCreateRequest();
            req.setIncidentId("id-1");
            req.setPhaseTypeId("phase-1");
            client.createIncidentPhase(req);

            assertEquals(true, capturedBody.toString().contains("id-1"));
            assertEquals(true, capturedBody.toString().contains("phase-1"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void getPhaseTypesThrowsOnNon200() throws Exception {
        HttpServer server = startServer(exchange -> respondJson(exchange, 500, "[]"));
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            assertThrows(RuntimeException.class, client::getPhaseTypes);
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startServer(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/incidents/phase/types", handler);
        server.createContext("/incident", handler);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        return server;
    }

    private void respondJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
