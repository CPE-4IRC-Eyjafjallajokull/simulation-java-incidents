package cpe.simulator.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import cpe.simulator.api.GeocodeService;
import cpe.simulator.domain.Location;
import cpe.simulator.infrastructure.http.HttpApiClient;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/** Service de géocodage inverse via l'API SDMIS. */
public final class SdmisGeocodeService implements GeocodeService {

  private final HttpApiClient httpClient;

  public SdmisGeocodeService(HttpApiClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public Optional<Location> reverseGeocode(double latitude, double longitude)
      throws IOException, InterruptedException {
    String path = String.format(Locale.ROOT, "/geocode/reverse?lat=%s&lon=%s", latitude, longitude);
    JsonNode root = httpClient.get(path, JsonNode.class);

    if (root == null || !root.path("ok").asBoolean(false)) {
      return Optional.empty();
    }

    JsonNode addr = root.path("data").path("address");
    if (addr.isMissingNode() || addr.isNull()) {
      return Optional.empty();
    }

    String houseNumber = textOrNull(addr, "house_number");
    String road = textOrNull(addr, "road");
    String postcode = textOrNull(addr, "postcode");
    String town =
        firstNonNull(
            textOrNull(addr, "town"),
            textOrNull(addr, "city"),
            textOrNull(addr, "municipality"),
            textOrNull(addr, "village"));

    String street = joinNonBlank(" ", houseNumber, road);
    if (street == null) {
      return Optional.empty();
    }

    return Optional.of(new Location(street, postcode, town, latitude, longitude));
  }

  private String textOrNull(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (value.isMissingNode() || value.isNull()) {
      return null;
    }
    String text = value.asText().trim();
    return text.isEmpty() ? null : text;
  }

  private String firstNonNull(String... values) {
    for (String v : values) {
      if (v != null) {
        return v;
      }
    }
    return null;
  }

  private String joinNonBlank(String separator, String... parts) {
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part != null && !part.isBlank()) {
        if (sb.length() > 0) {
          sb.append(separator);
        }
        sb.append(part);
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }
}
