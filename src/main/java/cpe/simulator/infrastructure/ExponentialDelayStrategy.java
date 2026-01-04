package cpe.simulator.infrastructure;

import cpe.simulator.api.DelayStrategy;
import java.util.Random;

/** Délai exponentiel basé sur un processus de Poisson. */
public final class ExponentialDelayStrategy implements DelayStrategy {

  private static final long MIN_DELAY_MS = 1_000L;
  private static final double MAX_DELAY_MULTIPLIER = 10.0;

  private final Random rng;
  private final double meanIntervalMs;

  public ExponentialDelayStrategy(double incidentsPerHour, long seed) {
    if (incidentsPerHour <= 0) {
      throw new IllegalArgumentException("incidentsPerHour must be > 0");
    }
    this.rng = new Random(seed);
    this.meanIntervalMs = 3_600_000.0 / incidentsPerHour;
  }

  @Override
  public long nextDelayMs() {
    double sample = -Math.log(1.0 - rng.nextDouble()) * meanIntervalMs;
    long maxDelayMs = Math.max(MIN_DELAY_MS, Math.round(meanIntervalMs * MAX_DELAY_MULTIPLIER));
    long delayMs = Math.round(sample);
    return Math.min(Math.max(delayMs, MIN_DELAY_MS), maxDelayMs);
  }
}
