package cpe.simulator.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Charge les probabilités d'évolution des incidents (phases) depuis sub-incident-probabilities.json. */
public class SubIncidentProbabilityLoader {
    private final Map<String, Map<String, Double>> probabilities;

    public SubIncidentProbabilityLoader(InputStream jsonStream) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Map incidentCode -> (nextPhaseCode -> probability)
        probabilities = mapper.readValue(jsonStream, HashMap.class);
    }

    public Map<String, Double> getNextPhases(String incidentCode) {
        return probabilities.getOrDefault(incidentCode, new HashMap<>());
    }
}
