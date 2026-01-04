package cpe.simulator.domain;

/** Zone géographique définie par des coordonnées min/max. */
public record GeoZone(
    double latitudeMin, double latitudeMax, double longitudeMin, double longitudeMax) {
  public GeoZone {
    if (latitudeMin > latitudeMax) {
      throw new IllegalArgumentException("latitudeMin must be <= latitudeMax");
    }
    if (longitudeMin > longitudeMax) {
      throw new IllegalArgumentException("longitudeMin must be <= longitudeMax");
    }
  }

  /** Génère une latitude aléatoire dans la zone. */
  public double randomLatitude(java.util.Random rng) {
    return latitudeMin + (latitudeMax - latitudeMin) * rng.nextDouble();
  }

  /** Génère une longitude aléatoire dans la zone. */
  public double randomLongitude(java.util.Random rng) {
    return longitudeMin + (longitudeMax - longitudeMin) * rng.nextDouble();
  }
}
