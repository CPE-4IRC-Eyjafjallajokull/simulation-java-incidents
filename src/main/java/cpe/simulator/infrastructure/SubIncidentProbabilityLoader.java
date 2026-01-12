package cpe.simulator.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/** Charge les probabilités d'évolution des incidents (phases) depuis sub-incident-probabilities.json. */
public final class SubIncidentProbabilityLoader {

    private static final TypeReference<Map<String, Map<String, Double>>> TYPE_REF =
            new TypeReference<>() {};

    private final Map<String, Map<String, Double>> probabilities;

    public SubIncidentProbabilityLoader(InputStream jsonStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.probabilities = mapper.readValue(jsonStream, TYPE_REF);
    }

    public Map<String, Double> getNextPhases(String incidentCode) {
        return probabilities.getOrDefault(incidentCode, Collections.emptyMap());
    }
}
