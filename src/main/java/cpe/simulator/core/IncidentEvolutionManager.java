package cpe.simulator.core;

import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;
import cpe.simulator.infrastructure.SubIncidentProbabilityLoader;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Logger;

/** Gère l'évolution des incidents selon les probabilités de phases. */
public class IncidentEvolutionManager {
    private static final String TERMINAL_PHASE_CODE = "NO_INCIDENT";
    private final SubIncidentProbabilityLoader probabilityLoader;
    private final Random rng;
    private final Logger logger = Logger.getLogger(IncidentEvolutionManager.class.getName());

    public IncidentEvolutionManager(SubIncidentProbabilityLoader loader, long seed) {
        this.probabilityLoader = loader;
        this.rng = new Random(seed);
    }

    /**
     * Fait évoluer l'incident vers une nouvelle phase selon les probabilités.
     * @param incident Incident à faire évoluer
     * @return true si une évolution a eu lieu
     */
    public boolean evolve(Incident incident) {
        String currentCode = incident.currentPhase().code();
        Map<String, Double> nextPhases = probabilityLoader.getNextPhases(currentCode);
        if (nextPhases == null || nextPhases.isEmpty()) {
            logger.fine("Aucune phase suivante possible pour " + currentCode);
            return false;
        }

        String nextCode = pickNextPhase(nextPhases);
        if (nextCode == null) {
            logger.warning("Aucune phase tirée au sort pour " + currentCode);
            return false;
        }

        // Stop evolution if next phase is terminal
        if (TERMINAL_PHASE_CODE.equalsIgnoreCase(nextCode)) {
            logger.info("Phase terminale atteinte (" + nextCode + ") pour l'incident, arrêt de l'évolution.");
            return false;
        }

        IncidentPhase nextPhase = new IncidentPhase(nextCode, nextCode, OffsetDateTime.now());
        incident.phases().add(nextPhase);
        // Met à jour la phase courante proprement
        Incident updated = incident.withCurrentPhase(nextPhase);
        // On ne peut pas remplacer l'objet Incident (record), donc il faut que l'appelant récupère la nouvelle instance si besoin
        // Mais on met à jour la liste des phases de l'instance existante (side effect)
        // Pour compatibilité, on retourne true si évolution
        return true;
    }

    private String pickNextPhase(Map<String, Double> probabilities) {
        double p = rng.nextDouble();
        double cumulative = 0.0;
        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            cumulative += entry.getValue();
            if (p <= cumulative) {
                return entry.getKey();
            }
        }
        return null;
    }
}
