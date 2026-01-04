package cpe.simulator.core;

import cpe.simulator.api.DelayStrategy;
import cpe.simulator.api.IncidentService;
import cpe.simulator.api.Logger;
import cpe.simulator.domain.Incident;
import java.util.Optional;

/** Boucle principale du simulateur d'incidents. */
public final class Simulator {

  private static final long ERROR_DELAY_MS = 1_000L;

  private final IncidentGenerator generator;
  private final IncidentService incidentService;
  private final DelayStrategy delayStrategy;
  private final Logger logger;

  public Simulator(
      IncidentGenerator generator,
      IncidentService incidentService,
      DelayStrategy delayStrategy,
      Logger logger) {
    this.generator = generator;
    this.incidentService = incidentService;
    this.delayStrategy = delayStrategy;
    this.logger = logger;
  }

  /** Lance la boucle de simulation (bloquant). */
  public void run() {
    logger.info("Boucle de simulation démarrée");

    while (!Thread.currentThread().isInterrupted()) {
      long delayMs = delayStrategy.nextDelayMs();
      logger.info("Prochain incident dans " + (delayMs / 1000.0) + "s");

      if (!sleep(delayMs)) {
        break;
      }

      try {
        processNextIncident();
      } catch (Exception e) {
        logger.error("Erreur pendant la simulation: " + e.getMessage());
        if (!sleep(ERROR_DELAY_MS)) {
          break;
        }
      }
    }

    logger.info("Simulation arrêtée");
  }

  private void processNextIncident() throws Exception {
    Optional<Incident> maybeIncident = generator.generate();

    if (maybeIncident.isEmpty()) {
      return;
    }

    Incident incident = maybeIncident.get();
    logIncidentStart(incident);

    String incidentId = incidentService.createIncident(incident);
    logger.info(
        "Incident envoyé: "
            + incident.code()
            + (incidentId != null ? " (" + incidentId + ")" : ""));
  }

  private void logIncidentStart(Incident incident) {
    if (incident.location() != null) {
      logger.info(
          "Envoi incident "
              + incident.code()
              + " (lat="
              + incident.location().latitude()
              + ", lon="
              + incident.location().longitude()
              + ")");
    } else {
      logger.info("Envoi incident " + incident.code());
    }
  }

  private boolean sleep(long delayMs) {
    try {
      Thread.sleep(delayMs);
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }
}
