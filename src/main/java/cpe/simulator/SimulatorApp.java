package cpe.simulator;

import java.util.Map;
import java.util.Set;

import cpe.simulator.api.IncidentApiClient;
import cpe.simulator.config.SimulatorConfig;
import cpe.simulator.loader.IncidentProbabilityLoader;
import cpe.simulator.model.Incident;
import cpe.simulator.rng.IncidentSelector;

public class SimulatorApp {

    public static void main(String[] args) throws Exception {

        Map<String, Double> probabilities =
                IncidentProbabilityLoader.load("incident-probabilities.json");

        IncidentSelector selector =
                new IncidentSelector(probabilities, 42L);

        String incidentCode = selector.pickIncidentCode();
        Incident incident = new Incident(incidentCode);

        IncidentApiClient api =
                new IncidentApiClient(
                        SimulatorConfig.API_BASE_URL,
                        SimulatorConfig.API_TOKEN
                );

        Set<String> validCodes = api.getValidIncidentCodes();

        if (validCodes.contains(incidentCode)) {
            api.postIncident(incident);
            System.out.println("✅ Incident envoyé : " + incidentCode);
        } else {
            System.out.println("⚠️ Incident ignoré (code invalide) : " + incidentCode);
        }
    }
}
