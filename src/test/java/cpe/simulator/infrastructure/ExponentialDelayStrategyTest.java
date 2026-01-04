package cpe.simulator.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExponentialDelayStrategyTest {

  @Test
  void generatesPositiveDelays() {
    ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(12.0, 42L);

    for (int i = 0; i < 100; i++) {
      long delay = strategy.nextDelayMs();
      assertTrue(delay >= 1000, "Delay should be at least 1 second");
    }
  }

  @Test
  void delaysAreWithinReasonableBounds() {
    ExponentialDelayStrategy strategy = new ExponentialDelayStrategy(12.0, 42L);
    // Mean interval = 3600000 / 12 = 300000 ms = 5 minutes
    // Max delay = 5 min * 10 = 50 minutes

    for (int i = 0; i < 100; i++) {
      long delay = strategy.nextDelayMs();
      assertTrue(delay <= 3_000_000L, "Delay should not exceed max bound");
    }
  }

  @Test
  void rejectsZeroIncidentsPerHour() {
    assertThrows(IllegalArgumentException.class, () -> new ExponentialDelayStrategy(0, 42L));
  }

  @Test
  void rejectsNegativeIncidentsPerHour() {
    assertThrows(IllegalArgumentException.class, () -> new ExponentialDelayStrategy(-1, 42L));
  }

  @Test
  void sameSeednProducesSameSequence() {
    ExponentialDelayStrategy s1 = new ExponentialDelayStrategy(12.0, 42L);
    ExponentialDelayStrategy s2 = new ExponentialDelayStrategy(12.0, 42L);

    for (int i = 0; i < 10; i++) {
      assertEquals(s1.nextDelayMs(), s2.nextDelayMs());
    }
  }
}
