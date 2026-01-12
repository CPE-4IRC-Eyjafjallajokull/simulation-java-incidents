package cpe.simulator.core;

import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;
import cpe.simulator.infrastructure.SubIncidentProbabilityLoader;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Random;

/** Gère l'évolution des incidents selon les probabilités de phases. */
public final class IncidentEvolutionManager {

    private final SubIncidentProbabilityLoader probabilityLoader;
    private final Random rng;

    public IncidentEvolutionManager(SubIncidentProbabilityLoader loader, long seed) {
        this.probabilityLoader = loader;
        this.rng = new Random(seed);
    }

    /**
     * Fait évoluer l'incident vers une nouvelle phase selon les probabilités.
     * Ajoute la nouvelle phase à la liste des phases de l'incident.
     *
     * @param incident Incident à faire évoluer (modifié in-place via sa liste de phases)
     * @return true si une évolution a eu lieu, false sinon
     */
    public boolean evolve(Incident incident) {
        String currentCode = incident.currentPhase().code();
        Map<String, Double> nextPhases = probabilityLoader.getNextPhases(currentCode);

        if (nextPhases == null || nextPhases.isEmpty()) {
            return false;
        }

        String nextCode = pickNextPhase(nextPhases);
        if (nextCode == null) {
            return false;
        }

        // Ajoute la nouvelle phase (y compris terminale) à la liste
        IncidentPhase nextPhase = new IncidentPhase(nextCode, nextCode, OffsetDateTime.now());
        incident.phases().add(nextPhase);
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
