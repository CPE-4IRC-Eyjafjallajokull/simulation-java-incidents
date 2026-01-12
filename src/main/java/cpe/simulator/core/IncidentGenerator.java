package cpe.simulator.core;

import cpe.simulator.api.GeocodeService;
import cpe.simulator.api.IncidentSelector;
import cpe.simulator.domain.GeoZone;
import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhase;
import cpe.simulator.domain.Location;
import cpe.simulator.domain.PhaseType;
import cpe.simulator.domain.PhaseTypeCatalog;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Génère des incidents aléatoires dans plusieurs zones géographiques. */
public final class IncidentGenerator {

  private static final int MAX_LOCATION_ATTEMPTS = 50;
  private static final int MAX_INCIDENT_ATTEMPTS = 10;

  private final IncidentSelector selector;
  private final PhaseTypeCatalog phaseCatalog;
  private final List<GeoZone> zones;
  private final GeocodeService geocodeService;
  private final Random locationRng;

  public IncidentGenerator(
      IncidentSelector selector,
      PhaseTypeCatalog phaseCatalog,
      List<GeoZone> zones,
      GeocodeService geocodeService,
      long seed) {
    this.selector = selector;
    this.phaseCatalog = phaseCatalog;
    this.zones = zones;
    this.geocodeService = geocodeService;
    this.locationRng = new Random(seed);
  }

  /** Constructeur pour une seule zone (rétrocompatibilité). */
  public IncidentGenerator(
      IncidentSelector selector,
      PhaseTypeCatalog phaseCatalog,
      GeoZone zone,
      GeocodeService geocodeService,
      long seed) {
    this(selector, phaseCatalog, List.of(zone), geocodeService, seed);
  }

  /**
   * Génère un incident aléatoire en respectant les zones exclues et restreintes.
   *
   * @return l'incident généré, ou vide si impossible après plusieurs tentatives
   */
  public Optional<Incident> generate() {
    for (int attempt = 0; attempt < MAX_INCIDENT_ATTEMPTS; attempt++) {
      String code = selector.pickIncidentCode();
      PhaseType phaseType = phaseCatalog.byCode(code);

      if (phaseType == null) {
        continue;
      }

      // Générer une location valide pour cet incident
      Optional<Location> maybeLocation = generateValidLocation(code);
      if (maybeLocation.isEmpty()) {
        continue;
      }

      Location location = maybeLocation.get();
      OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
      IncidentPhase initialPhase = new IncidentPhase(code, phaseType.label(), now);
      List<IncidentPhase> phases = new ArrayList<>();
      phases.add(initialPhase);

      Incident incident = Incident.builder()
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

    return Optional.empty();
  }

  /**
   * Génère une location valide pour un type d'incident donné.
   * Choisit aléatoirement une zone parmi toutes les zones disponibles.
   * Évite les zones exclues (eau) et vérifie les restrictions de zone.
   */
  private Optional<Location> generateValidLocation(String incidentCode) {
    for (int i = 0; i < MAX_LOCATION_ATTEMPTS; i++) {
      // Choisir une zone aléatoirement
      GeoZone zone = zones.get(locationRng.nextInt(zones.size()));
      
      double lat = zone.randomLatitude(locationRng);
      double lon = zone.randomLongitude(locationRng);

      // Vérifier si on est dans une zone exclue (eau) - vérifier toutes les zones
      if (isInAnyExcludedZone(lat, lon)) {
        continue;
      }

      // Vérifier si cet incident est interdit à cet endroit (parc)
      if (isIncidentForbiddenAnywhere(incidentCode, lat, lon)) {
        continue;
      }

      // Location valide trouvée
      Location base = Location.ofCoordinates(lat, lon);
      return Optional.of(resolveAddress(base, lat, lon));
    }

    return Optional.empty();
  }

  /** Vérifie si le point est dans une zone exclue de n'importe quelle zone. */
  private boolean isInAnyExcludedZone(double lat, double lon) {
    return zones.stream().anyMatch(z -> z.isInExcludedZone(lat, lon));
  }

  /** Vérifie si l'incident est interdit à cet endroit dans n'importe quelle zone. */
  private boolean isIncidentForbiddenAnywhere(String incidentCode, double lat, double lon) {
    return zones.stream().anyMatch(z -> z.isIncidentForbiddenAt(incidentCode, lat, lon));
  }

  private Location resolveAddress(Location base, double lat, double lon) {
    if (geocodeService == null) {
      return base;
    }

    try {
      return geocodeService
          .reverseGeocode(lat, lon)
          .map(resolved -> base.withAddress(resolved.address(), resolved.zipcode(), resolved.city()))
          .orElse(base);
    } catch (Exception e) {
      return base;
    }
  }
}
