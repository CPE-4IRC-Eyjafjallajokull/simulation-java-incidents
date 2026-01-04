package cpe.simulator.infrastructure;

import cpe.simulator.api.IncidentSelector;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/** Sélectionne un incident selon des probabilités pondérées. */
public final class ProbabilityBasedSelector implements IncidentSelector {

  private final Random random;
  private final NavigableMap<Double, String> distribution;
  private final double totalWeight;

  public ProbabilityBasedSelector(Map<String, Double> probabilities, long seed) {
    this.random = new Random(seed);
    this.distribution = new TreeMap<>();

    double cumulative = 0.0;
    for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
      if (entry.getValue() > 0) {
        cumulative += entry.getValue();
        distribution.put(cumulative, entry.getKey());
      }
    }
    this.totalWeight = cumulative;

    if (distribution.isEmpty()) {
      throw new IllegalArgumentException("No incident probabilities configured");
    }
  }

  @Override
  public String pickIncidentCode() {
    double r = random.nextDouble() * totalWeight;
    Map.Entry<Double, String> entry = distribution.higherEntry(r);
    if (entry == null) {
      return distribution.lastEntry().getValue();
    }
    return entry.getValue();
  }
}
