package cpe.simulator.core;

import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;
import cpe.simulator.domain.Location;
import cpe.simulator.infrastructure.SubIncidentProbabilityLoader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import cpe.simulator.api.IncidentService;
import cpe.simulator.api.Logger;
import cpe.simulator.api.DelayStrategy;

import java.io.FileInputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class SimulatorPhasePushTest {
    @Test
    void testPushPhasesToApi() throws Exception {
        // Mocks
        IncidentService incidentService = mock(IncidentService.class);
        Logger logger = mock(Logger.class);
        DelayStrategy delayStrategy = mock(DelayStrategy.class);
        when(delayStrategy.nextDelayMs()).thenReturn(0L);
        when(incidentService.createIncident(any())).thenReturn("id");

        // Loader et manager
        SubIncidentProbabilityLoader loader = new SubIncidentProbabilityLoader(
                new FileInputStream("src/main/resources/sub-incident-probabilities.json"));
        IncidentEvolutionManager manager = new IncidentEvolutionManager(loader, 42L);

        // IncidentSelector mocké pour retourner le code voulu
        cpe.simulator.api.IncidentSelector selector = mock(cpe.simulator.api.IncidentSelector.class);
        when(selector.pickIncidentCode()).thenReturn("SAP_CARDIAC_ARREST");

        // PhaseTypeCatalog réel
        cpe.simulator.domain.PhaseType phaseType = new cpe.simulator.domain.PhaseType("testId", "catId", "SAP_CARDIAC_ARREST", "Arrêt cardiaque", 1);
        java.util.List<cpe.simulator.domain.PhaseType> phaseTypes = java.util.List.of(phaseType);
        cpe.simulator.domain.PhaseTypeCatalog phaseCatalog = new cpe.simulator.domain.PhaseTypeCatalog(phaseTypes);

        // GeoZone réel (valeurs arbitraires)
        cpe.simulator.domain.GeoZone zone = new cpe.simulator.domain.GeoZone(0.0, 0.0, 0.0, 0.0);
        // GeocodeService mocké (interface, donc OK)
        cpe.simulator.api.GeocodeService geocodeService = mock(cpe.simulator.api.GeocodeService.class);

        // Generator réel
        IncidentGenerator generator = new IncidentGenerator(selector, phaseCatalog, zone, geocodeService, 42L);

        // Simulator
        Simulator simulator = new Simulator(generator, incidentService, delayStrategy, logger, manager, phaseCatalog, 42L);
        simulator.runOnce();

        // Vérifie que l'API a été appelée pour l'incident initial
        verify(incidentService, atLeast(1)).createIncident(any());
        // Vérifie que l'API a été appelée pour l'ajout de phase (évolution)
        verify(incidentService, atLeast(0)).addIncidentPhase(anyString(), any());
    }
}
