package cpe.simulator.core;

import cpe.simulator.api.GeocodeService;
import cpe.simulator.api.IncidentSelector;
import cpe.simulator.domain.GeoZone;
import cpe.simulator.domain.Incident;
import cpe.simulator.domain.Location;
import cpe.simulator.domain.PhaseType;
import cpe.simulator.domain.PhaseTypeCatalog;
import cpe.simulator.domain.IncidentPhase;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Random;

/** Génère des incidents aléatoires dans une zone géographique. */
public final class IncidentGenerator {

  private final IncidentSelector selector;
  private final PhaseTypeCatalog phaseCatalog;
  private final GeoZone zone;
  private final GeocodeService geocodeService;
  private final Random locationRng;

  public IncidentGenerator(
      IncidentSelector selector,
      PhaseTypeCatalog phaseCatalog,
      GeoZone zone,
      GeocodeService geocodeService,
      long seed) {
    this.selector = selector;
    this.phaseCatalog = phaseCatalog;
    this.zone = zone;
    this.geocodeService = geocodeService;
    this.locationRng = new Random(seed);
  }

  /**
   * Génère un incident aléatoire.
   *
   * @return l'incident généré, ou vide si le code est invalide
   */
  public Optional<Incident> generate() {
    String code = selector.pickIncidentCode();
    PhaseType phaseType = phaseCatalog.byCode(code);

    if (phaseType == null) {
      return Optional.empty();
    }

    Location location = generateLocation();
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    IncidentPhase initialPhase = new IncidentPhase(code, phaseType.label(), now);
    java.util.List<IncidentPhase> phases = new java.util.ArrayList<>();
    phases.add(initialPhase);

    Incident incident =
      Incident.builder()
        .code(code)
        .label(phaseType.label())
        .description("Incident " + phaseType.label() + " simulé")
        .location(location)
        .phaseType(phaseType)
        .startedAt(now)
        .phases(phases)
        .currentPhase(initialPhase)
        .build();

    return Optional.of(incident);
  }

  private Location generateLocation() {
    double lat = zone.randomLatitude(locationRng);
    double lon = zone.randomLongitude(locationRng);
    Location base = Location.ofCoordinates(lat, lon);

    if (geocodeService == null) {
      return base;
    }

    try {
      return geocodeService
          .reverseGeocode(lat, lon)
          .map(
              resolved -> base.withAddress(resolved.address(), resolved.zipcode(), resolved.city()))
          .orElse(base);
    } catch (Exception e) {
      return base;
    }
  }
}
