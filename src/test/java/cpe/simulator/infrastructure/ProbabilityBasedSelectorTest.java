package cpe.simulator.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ProbabilityBasedSelectorTest {

  @Test
  void selectsIncidentBasedOnProbabilities() {
    Map<String, Double> probabilities =
        Map.of(
            "FIRE", 0.5,
            "SAP", 0.3,
            "ACC", 0.2);
    ProbabilityBasedSelector selector = new ProbabilityBasedSelector(probabilities, 42L);

    // Avec une seed fixe, les résultats sont déterministes
    String first = selector.pickIncidentCode();
    assertNotNull(first);
    assertTrue(probabilities.containsKey(first));
  }

  @Test
  void selectsAllTypesOverManyIterations() {
    Map<String, Double> probabilities =
        Map.of(
            "FIRE", 0.33,
            "SAP", 0.33,
            "ACC", 0.34);
    ProbabilityBasedSelector selector = new ProbabilityBasedSelector(probabilities, 123L);

    Map<String, Integer> counts = new java.util.HashMap<>();
    for (int i = 0; i < 1000; i++) {
      String code = selector.pickIncidentCode();
      counts.merge(code, 1, Integer::sum);
    }

    // Chaque type devrait être sélectionné au moins une fois
    assertEquals(3, counts.size());
    assertTrue(counts.get("FIRE") > 0);
    assertTrue(counts.get("SAP") > 0);
    assertTrue(counts.get("ACC") > 0);
  }

  @Test
  void rejectsEmptyProbabilities() {
    assertThrows(IllegalArgumentException.class, () -> new ProbabilityBasedSelector(Map.of(), 42L));
  }

  @Test
  void ignoresZeroOrNegativeProbabilities() {
    Map<String, Double> probabilities =
        Map.of(
            "FIRE", 1.0,
            "SAP", 0.0,
            "ACC", -0.5);
    ProbabilityBasedSelector selector = new ProbabilityBasedSelector(probabilities, 42L);

    // Seul FIRE devrait être sélectionnable
    for (int i = 0; i < 10; i++) {
      assertEquals("FIRE", selector.pickIncidentCode());
    }
  }
}
