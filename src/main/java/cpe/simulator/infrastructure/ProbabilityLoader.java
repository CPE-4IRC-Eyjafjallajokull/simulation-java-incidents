package cpe.simulator.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Charge les probabilités d'incidents depuis un fichier JSON. */
public final class ProbabilityLoader {

  private static final String NEUTRAL_CODE = "NO_INCIDENT";
  private final ObjectMapper mapper = new ObjectMapper();

  /** Charge les probabilités en excluant NO_INCIDENT. */
  public Map<String, Double> load(String path) throws IOException {
    try (InputStream input = ResourceLoader.open(path)) {
      Map<String, Double> all = mapper.readValue(input, new TypeReference<>() {});
      all.remove(NEUTRAL_CODE);
      return all;
    }
  }
}
