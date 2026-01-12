package cpe.simulator.core;

import cpe.simulator.api.DelayStrategy;
import cpe.simulator.api.IncidentService;
import cpe.simulator.api.Logger;
import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;

import java.util.*;

/** Boucle principale du simulateur d'incidents. */
public final class Simulator {
  // Liste des incidents actifs (non terminés)
  private final List<IncidentWithId> activeIncidents = new ArrayList<>();
  private boolean createNext = true; // alterne création/évolution

  // Structure pour suivre l'incident et son id API
  private static class IncidentWithId {
    Incident incident;
    String incidentId;
    boolean finished;
    IncidentWithId(Incident incident, String incidentId) {
      this.incident = incident;
      this.incidentId = incidentId;
      this.finished = false;
    }
  }
  /** Exécute une seule itération de simulation (pour les tests). */
  public void runOnce() throws Exception {
    processNextIncident();
  }

  private static final long ERROR_DELAY_MS = 1_000L;

  private final IncidentGenerator generator;
  private final IncidentService incidentService;
  private final DelayStrategy delayStrategy;
  private final Logger logger;
  private final IncidentEvolutionManager evolutionManager;
  private final cpe.simulator.domain.PhaseTypeCatalog phaseCatalog;

  public Simulator(
      IncidentGenerator generator,
      IncidentService incidentService,
      DelayStrategy delayStrategy,
      Logger logger,
      IncidentEvolutionManager evolutionManager,
      cpe.simulator.domain.PhaseTypeCatalog phaseCatalog) {
    this.generator = generator;
    this.incidentService = incidentService;
    this.delayStrategy = delayStrategy;
    this.logger = logger;
    this.evolutionManager = evolutionManager;
    this.phaseCatalog = phaseCatalog;
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
    final String TERMINAL_PHASE_CODE = "NO_INCIDENT";
    if (createNext || activeIncidents.isEmpty()) {
      // Création d'un nouvel incident
      Optional<Incident> maybeIncident = generator.generate();
      if (maybeIncident.isEmpty()) {
        logger.info("Aucun incident généré à ce tic.");
        createNext = false;
        return;
      }
      Incident incident = maybeIncident.get();
      logIncidentStart(incident);
      String incidentId = incidentService.createIncident(incident);
      logger.info("Incident envoyé: " + incident.label() + (incidentId != null ? " (" + incidentId + ")" : ""));
      activeIncidents.add(new IncidentWithId(incident, incidentId));
      createNext = false;
    } else {
      // Évolution d'un incident existant (choisi au hasard parmi les non terminés)
      List<IncidentWithId> candidates = new ArrayList<>();
      for (IncidentWithId iwid : activeIncidents) {
        if (!iwid.finished) {
          candidates.add(iwid);
        }
      }
      if (candidates.isEmpty()) {
        logger.info("Aucun incident actif à faire évoluer à ce tic.");
        createNext = true;
        return;
      }
      IncidentWithId toEvolve = candidates.get(new Random().nextInt(candidates.size()));
      Incident evolvingIncident = toEvolve.incident;
      boolean evolved = evolutionManager.evolve(evolvingIncident);
      if (evolved) {
        IncidentPhase lastPhase = evolvingIncident.phases().get(evolvingIncident.phases().size() - 1);
        toEvolve.incident = evolvingIncident.withCurrentPhase(lastPhase);
        String phaseCode = lastPhase.code();
        logger.info("Incident " + toEvolve.incident.label() + " evolution : nouvelle phase " + phaseCode);
        if (!TERMINAL_PHASE_CODE.equalsIgnoreCase(phaseCode)) {
          cpe.simulator.domain.PhaseType phaseType = phaseCatalog.byCode(phaseCode);
          String phaseTypeId = phaseType != null ? phaseType.phaseTypeId() : phaseCode;
          int priority = phaseType != null ? phaseType.defaultCriticity() : 0;
          java.time.OffsetDateTime startedAt = lastPhase.timestamp();
          java.time.OffsetDateTime endedAt = startedAt;
          incidentService.addIncidentPhase(toEvolve.incidentId,
            new cpe.simulator.domain.IncidentPhaseForApi(phaseTypeId, priority, startedAt, endedAt));
          logger.info("Nouvelle phase envoyée: " + phaseCode + " (phase_type_id=" + phaseTypeId + ")");
        } else {
          logger.info("Incident terminé (NO_INCIDENT) : " + toEvolve.incident.label());
          toEvolve.finished = true;
        }
      } else {
        logger.info("Aucune évolution possible pour l'incident : " + toEvolve.incident.label());
      }
      createNext = true;
    }
  }

  private void logIncidentStart(Incident incident) {
    if (incident.location() != null) {
      logger.info(
          "Envoi incident "
              + incident.label()
              + " (lat="
              + incident.location().latitude()
              + ", lon="
              + incident.location().longitude()
              + ")");
    } else {
      logger.info("Envoi incident " + incident.label());
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
