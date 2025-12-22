package cpe.simulator.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class IncidentProbabilityLoader {

    public static Map<String, Double> load(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(
                new File(path),
                new TypeReference<Map<String, Double>>() {}
        );
    }
}
