package cpe.simulator;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

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
                new IncidentSelector(probabilities, SimulatorConfig.RNG_SEED);

        String incidentCode = selector.pickIncidentCode();

        handleIncidentCode(incidentCode, () ->
                new IncidentApiClient(
                        SimulatorConfig.API_BASE_URL,
                        SimulatorConfig.API_TOKEN
                )
        );
    }

    static void handleIncidentCode(String incidentCode,
                                   Supplier<IncidentApiClient> apiSupplier)
            throws Exception {

        if ("000".equals(incidentCode)) {
            System.out.println("ℹ️ Incident neutre ignoré : " + incidentCode);
            return;
        }

        Incident incident = new Incident(incidentCode);

        IncidentApiClient api = apiSupplier.get();
        Set<String> validCodes = api.getValidIncidentCodes();

        if (validCodes.contains(incidentCode)) {
            api.postIncident(incident);
            System.out.println("✅ Incident envoyé : " + incidentCode);
        } else {
            System.out.println("⚠️ Incident ignoré (code invalide) : " + incidentCode);
        }
    }
}
