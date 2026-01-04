package cpe.simulator.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProbabilityLoaderTest {

  @Test
  void loadsProbabilitiesFromClasspath() throws IOException {
    ProbabilityLoader loader = new ProbabilityLoader();
    Map<String, Double> probs = loader.load("incident-probabilities.json");

    assertFalse(probs.isEmpty());
    assertTrue(probs.containsKey("FIRE_APARTMENT"));
    assertTrue(probs.containsKey("SAP_CARDIAC_ARREST"));
  }

  @Test
  void excludesNoIncident() throws IOException {
    ProbabilityLoader loader = new ProbabilityLoader();
    Map<String, Double> probs = loader.load("incident-probabilities.json");

    assertFalse(probs.containsKey("NO_INCIDENT"));
  }

  @Test
  void throwsForMissingFile() {
    ProbabilityLoader loader = new ProbabilityLoader();

    assertThrows(IOException.class, () -> loader.load("nonexistent.json"));
  }
}
