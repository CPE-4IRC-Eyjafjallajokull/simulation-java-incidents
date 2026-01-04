package cpe.simulator.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IncidentProbabilityLoaderTest {

    @Test
    void loadsProbabilitiesFromJsonFile() throws Exception {
        Path tempFile = Files.createTempFile("incident-probabilities", ".json");
        Files.writeString(tempFile, "{\"A\":0.5,\"B\":0.25,\"C\":0.25}");

        Map<String, Double> probabilities = IncidentProbabilityLoader.load(tempFile.toString());

        assertNotNull(probabilities);
        assertEquals(3, probabilities.size());
        assertEquals(0.5, probabilities.get("A"));
        assertEquals(0.25, probabilities.get("B"));
        assertEquals(0.25, probabilities.get("C"));
    }
}
