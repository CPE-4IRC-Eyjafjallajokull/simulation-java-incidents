package cpe.simulator.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cpe.simulator.model.Incident;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class IncidentApiClientTest {

    @Test
    void getValidIncidentCodesReturnsServerValues() throws Exception {
        HttpServer server = startServer(exchange -> respondJson(exchange, 200, "[\"X\",\"Y\"]"));
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            Set<String> codes = client.getValidIncidentCodes();

            assertEquals(Set.of("X", "Y"), codes);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void postIncidentSendsBodyAndSucceedsOn201() throws Exception {
        StringBuilder capturedBody = new StringBuilder();
        HttpServer server = startServer(exchange -> {
            capturedBody.append(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respondJson(exchange, 201, "{} ");
        });
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            Incident incident = new Incident("CODE1");
            incident = stripOccurredAt(incident);

            client.postIncident(incident);

            assertEquals(true, capturedBody.toString().contains("CODE1"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void getValidIncidentCodesThrowsOnNon200() throws Exception {
        HttpServer server = startServer(exchange -> respondJson(exchange, 500, "[]"));
        int port = server.getAddress().getPort();

        try {
            IncidentApiClient client = new IncidentApiClient("http://localhost:" + port, "token");

            assertThrows(RuntimeException.class, client::getValidIncidentCodes);
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

    private Incident stripOccurredAt(Incident incident) throws Exception {
        var field = Incident.class.getDeclaredField("occurredAt");
        field.setAccessible(true);
        field.set(incident, null);
        return incident;
    }
}
