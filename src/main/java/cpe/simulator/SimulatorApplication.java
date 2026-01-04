package cpe.simulator;

import cpe.simulator.api.Logger;
import cpe.simulator.config.SimulatorConfig;
import cpe.simulator.core.Simulator;
import cpe.simulator.infrastructure.ConsoleLogger;

/** Point d'entrée du simulateur d'incidents. */
public final class SimulatorApplication {

  public static void main(String[] args) {
    Logger logger = new ConsoleLogger();

    try {
      logger.info("Démarrage du simulateur");

      SimulatorConfig config = SimulatorConfig.fromEnvironment();
      Simulator simulator = SimulatorFactory.create(config, logger);
      simulator.run();

    } catch (Exception e) {
      logger.error("Erreur fatale: " + e.getMessage());
      System.exit(1);
    }
  }
}
