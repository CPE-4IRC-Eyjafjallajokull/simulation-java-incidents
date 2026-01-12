package cpe.simulator.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpe.simulator.domain.ExcludedZone;
import cpe.simulator.domain.GeoZone;
import cpe.simulator.domain.Polygon;
import cpe.simulator.domain.RestrictedZone;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Charge les zones géographiques depuis un fichier JSON. */
public final class GeoZoneLoader {

  private final ObjectMapper mapper = new ObjectMapper();

  /** Charge une zone spécifique par nom. */
  public GeoZone load(String path, String zoneName) throws IOException {
    try (InputStream input = ResourceLoader.open(path)) {
      JsonNode root = mapper.readTree(input);
      JsonNode zoneNode = root.path("zones").path(zoneName);

      if (zoneNode.isMissingNode()) {
        throw new IllegalArgumentException("Zone not found: " + zoneName);
      }

      // Charger les zones exclues globales
      List<ExcludedZone> globalExcluded = loadExcludedZones(root.path("globalExcludedZones"));

      return loadZoneFromNode(zoneNode, globalExcluded);
    }
  }

  /** Charge toutes les zones disponibles. */
  public List<GeoZone> loadAll(String path) throws IOException {
    try (InputStream input = ResourceLoader.open(path)) {
      JsonNode root = mapper.readTree(input);
      JsonNode zonesNode = root.path("zones");

      // Charger les zones exclues globales
      List<ExcludedZone> globalExcluded = loadExcludedZones(root.path("globalExcludedZones"));

      List<GeoZone> zones = new ArrayList<>();
      zonesNode.fieldNames().forEachRemaining(zoneName -> {
        JsonNode zoneNode = zonesNode.path(zoneName);
        zones.add(loadZoneFromNode(zoneNode, globalExcluded));
      });
      return zones;
    }
  }

  private GeoZone loadZoneFromNode(JsonNode zoneNode, List<ExcludedZone> globalExcluded) {
    // Charger les bounds
    JsonNode boundsNode = zoneNode.path("bounds");
    double latMin = boundsNode.path("latMin").asDouble();
    double latMax = boundsNode.path("latMax").asDouble();
    double lonMin = boundsNode.path("lonMin").asDouble();
    double lonMax = boundsNode.path("lonMax").asDouble();

    // Fallback pour l'ancien format
    if (latMin == 0 && latMax == 0) {
      latMin = zoneNode.path("latitude").path("min").asDouble();
      latMax = zoneNode.path("latitude").path("max").asDouble();
      lonMin = zoneNode.path("longitude").path("min").asDouble();
      lonMax = zoneNode.path("longitude").path("max").asDouble();
    }

    // Charger les zones exclues (locales + globales)
    List<ExcludedZone> excludedZones = new ArrayList<>(globalExcluded);
    excludedZones.addAll(loadExcludedZones(zoneNode.path("excludedZones")));

    // Charger les zones restreintes
    List<RestrictedZone> restrictedZones = loadRestrictedZones(zoneNode.path("restrictedZones"));

    return new GeoZone(latMin, latMax, lonMin, lonMax, excludedZones, restrictedZones);
  }

  private List<ExcludedZone> loadExcludedZones(JsonNode excludedNode) {
    List<ExcludedZone> zones = new ArrayList<>();
    if (excludedNode.isMissingNode() || !excludedNode.isArray()) {
      return zones;
    }

    for (JsonNode node : excludedNode) {
      String type = node.path("type").asText();
      String name = node.path("name").asText();
      Polygon polygon = loadPolygon(node.path("polygon"));
      zones.add(new ExcludedZone(type, name, polygon));
    }
    return zones;
  }

  private List<RestrictedZone> loadRestrictedZones(JsonNode restrictedNode) {
    List<RestrictedZone> zones = new ArrayList<>();
    if (restrictedNode.isMissingNode() || !restrictedNode.isArray()) {
      return zones;
    }

    for (JsonNode node : restrictedNode) {
      String type = node.path("type").asText();
      String name = node.path("name").asText();
      Polygon polygon = loadPolygon(node.path("polygon"));
      List<String> forbiddenIncidents = loadForbiddenIncidents(node.path("forbiddenIncidents"));
      zones.add(new RestrictedZone(type, name, polygon, forbiddenIncidents));
    }
    return zones;
  }

  private Polygon loadPolygon(JsonNode polygonNode) {
    List<double[]> points = new ArrayList<>();
    if (polygonNode.isArray()) {
      for (JsonNode pointNode : polygonNode) {
        if (pointNode.isArray() && pointNode.size() >= 2) {
          double lat = pointNode.get(0).asDouble();
          double lon = pointNode.get(1).asDouble();
          points.add(new double[]{lat, lon});
        }
      }
    }
    return new Polygon(points);
  }

  private List<String> loadForbiddenIncidents(JsonNode forbiddenNode) {
    List<String> incidents = new ArrayList<>();
    if (forbiddenNode.isArray()) {
      for (JsonNode node : forbiddenNode) {
        incidents.add(node.asText());
      }
    }
    return incidents;
  }
}
