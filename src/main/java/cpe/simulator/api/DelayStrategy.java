package cpe.simulator.api;

/** Stratégie de calcul du délai entre incidents. */
public interface DelayStrategy {

  /**
   * @return le délai avant le prochain incident en millisecondes
   */
  long nextDelayMs();
}
