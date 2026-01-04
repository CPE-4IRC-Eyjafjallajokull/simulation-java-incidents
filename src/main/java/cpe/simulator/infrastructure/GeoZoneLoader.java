package cpe.simulator.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpe.simulator.domain.GeoZone;
import java.io.IOException;
import java.io.InputStream;

/** Charge les zones géographiques depuis un fichier JSON. */
public final class GeoZoneLoader {

  private final ObjectMapper mapper = new ObjectMapper();

  public GeoZone load(String path, String zoneName) throws IOException {
    try (InputStream input = ResourceLoader.open(path)) {
      JsonNode root = mapper.readTree(input);
      JsonNode zoneNode = root.path("zones").path(zoneName);

      if (zoneNode.isMissingNode()) {
        throw new IllegalArgumentException("Zone not found: " + zoneName);
      }

      return new GeoZone(
          zoneNode.path("latitude").path("min").asDouble(),
          zoneNode.path("latitude").path("max").asDouble(),
          zoneNode.path("longitude").path("min").asDouble(),
          zoneNode.path("longitude").path("max").asDouble());
    }
  }
}
