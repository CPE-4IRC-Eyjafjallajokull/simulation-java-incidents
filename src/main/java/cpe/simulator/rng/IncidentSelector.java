package cpe.simulator.rng;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class IncidentSelector {

    private final Random random;
    private final NavigableMap<Double, String> distribution = new TreeMap<>();
    private double totalWeight = 0.0;

    public IncidentSelector(Map<String, Double> probabilities, long seed) {
        this.random = new Random(seed);

        for (Entry<String, Double> entry : probabilities.entrySet()) {
            if (entry.getValue() <= 0) continue;
            totalWeight += entry.getValue();
            distribution.put(totalWeight, entry.getKey());
        }
    }

    public String pickIncidentCode() {
        double r = random.nextDouble() * totalWeight;
        return distribution.higherEntry(r).getValue();
    }
}
