package cpe.simulator.rng;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IncidentSelectorTest {

    @Test
    void picksDeterministicIncidentWithSeed() {
        Map<String, Double> probabilities = new LinkedHashMap<>();
        probabilities.put("A", 0.7);
        probabilities.put("B", 0.3);

        IncidentSelector selector = new IncidentSelector(probabilities, 42L);

        String chosen = selector.pickIncidentCode();

        assertEquals("B", chosen);
    }
}
