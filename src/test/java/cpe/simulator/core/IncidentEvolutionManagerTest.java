package cpe.simulator.core;

import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;
import cpe.simulator.domain.Location;
import cpe.simulator.domain.PhaseType;
import cpe.simulator.infrastructure.SubIncidentProbabilityLoader;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncidentEvolutionManagerTest {
    @Test
    void testIncidentEvolution() throws Exception {
        // Charge les probabilités de test
        SubIncidentProbabilityLoader loader = new SubIncidentProbabilityLoader(
                new FileInputStream("src/main/resources/sub-incident-probabilities.json"));
        IncidentEvolutionManager manager = new IncidentEvolutionManager(loader, 42L);

        // Incident initial
        IncidentPhase initialPhase = new IncidentPhase("SAP_CARDIAC_ARREST", "Arrêt cardiaque", OffsetDateTime.now());
        List<IncidentPhase> phases = new ArrayList<>();
        phases.add(initialPhase);
        Incident incident = Incident.builder()
                .code("SAP_CARDIAC_ARREST")
                .label("Arrêt cardiaque")
                .description("Test incident")
                .location(Location.ofCoordinates(0,0))
                .phaseType(new PhaseType("testId", "catId", "code", "label", 1))
                .priority(1)
                .startedAt(OffsetDateTime.now())
                .phases(phases)
                .currentPhase(initialPhase)
                .build();

        boolean evolved = manager.evolve(incident);
        // L'évolution peut ne pas se produire si NO_INCIDENT est tiré (proba 0.85)
        if (evolved) {
            assertEquals(2, incident.phases().size(), "Il doit y avoir 2 phases après évolution");
            assertNotNull(incident.currentPhase(), "La phase courante doit être définie");
        } else {
            assertEquals(1, incident.phases().size(), "Aucune évolution, une seule phase");
        }
    }
}
