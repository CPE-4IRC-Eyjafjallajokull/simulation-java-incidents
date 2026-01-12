package cpe.simulator.domain;

import java.util.List;
import java.util.Random;

/** Zone géographique définie par des coordonnées min/max avec zones exclues et restreintes. */
public record GeoZone(
    double latitudeMin,
    double latitudeMax,
    double longitudeMin,
    double longitudeMax,
    List<ExcludedZone> excludedZones,
    List<RestrictedZone> restrictedZones
) {

  /** Constructeur pour compatibilité arrière (sans zones exclues/restreintes). */
  public GeoZone(double latitudeMin, double latitudeMax, double longitudeMin, double longitudeMax) {
    this(latitudeMin, latitudeMax, longitudeMin, longitudeMax, List.of(), List.of());
  }

  public GeoZone {
    if (latitudeMin > latitudeMax) {
      throw new IllegalArgumentException("latitudeMin must be <= latitudeMax");
    }
    if (longitudeMin > longitudeMax) {
      throw new IllegalArgumentException("longitudeMin must be <= longitudeMax");
    }
    if (excludedZones == null) {
      excludedZones = List.of();
    }
    if (restrictedZones == null) {
      restrictedZones = List.of();
    }
  }

  /** Génère une latitude aléatoire dans la zone. */
  public double randomLatitude(Random rng) {
    return latitudeMin + (latitudeMax - latitudeMin) * rng.nextDouble();
  }

  /** Génère une longitude aléatoire dans la zone. */
  public double randomLongitude(Random rng) {
    return longitudeMin + (longitudeMax - longitudeMin) * rng.nextDouble();
  }

  /** Vérifie si un point est dans une zone exclue (eau, etc.). */
  public boolean isInExcludedZone(double latitude, double longitude) {
    return excludedZones.stream()
        .anyMatch(zone -> zone.contains(latitude, longitude));
  }

  /** Vérifie si un incident est interdit à un point donné. */
  public boolean isIncidentForbiddenAt(String incidentCode, double latitude, double longitude) {
    return restrictedZones.stream()
        .filter(zone -> zone.contains(latitude, longitude))
        .anyMatch(zone -> zone.isIncidentForbidden(incidentCode));
  }

  /** Retourne le nom de la zone restreinte si le point est dedans, null sinon. */
  public String getRestrictedZoneName(double latitude, double longitude) {
    return restrictedZones.stream()
        .filter(zone -> zone.contains(latitude, longitude))
        .findFirst()
        .map(RestrictedZone::name)
        .orElse(null);
  }
}
