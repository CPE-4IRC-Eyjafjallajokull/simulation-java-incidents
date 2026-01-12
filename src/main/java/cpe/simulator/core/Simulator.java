package cpe.simulator.core;

import cpe.simulator.api.DelayStrategy;
import cpe.simulator.api.IncidentService;
import cpe.simulator.api.Logger;
import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;
import cpe.simulator.domain.IncidentPhaseForApi;
import cpe.simulator.domain.PhaseType;
import cpe.simulator.domain.PhaseTypeCatalog;

import java.time.OffsetDateTime;
import java.util.*;

/** Boucle principale du simulateur d'incidents. */
public final class Simulator {

  private static final String TERMINAL_PHASE_CODE = "NO_INCIDENT";
  private static final long ERROR_DELAY_MS = 1_000L;

  private final List<IncidentWithId> activeIncidents = new ArrayList<>();
  private final Random selectionRng;
  private boolean createNext = true;

  private final IncidentGenerator generator;
  private final IncidentService incidentService;
  private final DelayStrategy delayStrategy;
  private final Logger logger;
  private final IncidentEvolutionManager evolutionManager;
  private final PhaseTypeCatalog phaseCatalog;

  /** Structure interne pour suivre l'incident et son id API. */
  private static final class IncidentWithId {
    private Incident incident;
    private final String incidentId;
    private boolean finished;

    IncidentWithId(Incident incident, String incidentId) {
      this.incident = incident;
      this.incidentId = incidentId;
    }
  }

  public Simulator(
      IncidentGenerator generator,
      IncidentService incidentService,
      DelayStrategy delayStrategy,
      Logger logger,
      IncidentEvolutionManager evolutionManager,
      PhaseTypeCatalog phaseCatalog,
      long rngSeed) {
    this.generator = Objects.requireNonNull(generator);
    this.incidentService = Objects.requireNonNull(incidentService);
    this.delayStrategy = Objects.requireNonNull(delayStrategy);
    this.logger = Objects.requireNonNull(logger);
    this.evolutionManager = Objects.requireNonNull(evolutionManager);
    this.phaseCatalog = Objects.requireNonNull(phaseCatalog);
    this.selectionRng = new Random(rngSeed);
  }

  /** Exécute une seule itération de simulation (pour les tests). */
  public void runOnce() throws Exception {
    processNextIncident();
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
    if (createNext || activeIncidents.isEmpty()) {
      createNewIncident();
    } else {
      evolveExistingIncident();
    }
  }

  private void createNewIncident() throws Exception {
    Optional<Incident> maybeIncident = generator.generate();
    if (maybeIncident.isEmpty()) {
      logger.info("Aucun incident généré à ce tic.");
      createNext = false;
      return;
    }

    Incident incident = maybeIncident.get();
    logIncidentStart(incident);
    String incidentId = incidentService.createIncident(incident);

    String idSuffix = incidentId != null ? " (" + incidentId + ")" : "";
    logger.info("NEW INCIDENT : " + incident.label() + idSuffix);

    activeIncidents.add(new IncidentWithId(incident, incidentId));
    createNext = false;
  }

  private void evolveExistingIncident() throws Exception {
    List<IncidentWithId> candidates = activeIncidents.stream()
        .filter(i -> !i.finished)
        .toList();

    if (candidates.isEmpty()) {
      logger.info("Aucun incident actif à faire évoluer à ce tic.");
      createNext = true;
      return;
    }

    IncidentWithId toEvolve = candidates.get(selectionRng.nextInt(candidates.size()));
    boolean evolved = evolutionManager.evolve(toEvolve.incident);

    if (evolved) {
      handleEvolution(toEvolve);
    } else {
      logger.info("Aucune évolution possible pour l'incident : " + toEvolve.incident.label());
    }
    createNext = true;
  }

  private void handleEvolution(IncidentWithId toEvolve) throws Exception {
    List<IncidentPhase> phases = toEvolve.incident.phases();
    IncidentPhase lastPhase = phases.get(phases.size() - 1);
    toEvolve.incident = toEvolve.incident.withCurrentPhase(lastPhase);

    String phaseCode = lastPhase.code();
    logger.info("EVOLUTION Incident " + toEvolve.incident.label() + " : + " + phaseCode);

    if (TERMINAL_PHASE_CODE.equalsIgnoreCase(phaseCode)) {
      logger.info("END Incident : " + toEvolve.incident.label());
      toEvolve.finished = true;
    } else {
      pushPhaseToApi(toEvolve.incidentId, lastPhase, phaseCode);
    }
  }

  private void pushPhaseToApi(String incidentId, IncidentPhase phase, String phaseCode)
      throws Exception {
    PhaseType phaseType = phaseCatalog.byCode(phaseCode);
    String phaseTypeId = phaseType != null ? phaseType.phaseTypeId() : phaseCode;
    int priority = phaseType != null ? phaseType.defaultCriticity() : 0;
    OffsetDateTime startedAt = phase.timestamp();

    incidentService.addIncidentPhase(incidentId,
        new IncidentPhaseForApi(phaseTypeId, priority, startedAt, startedAt));
    logger.info("NEW PHASE : " + phaseCode);
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
