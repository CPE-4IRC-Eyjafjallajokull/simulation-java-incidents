package cpe.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cpe.simulator.api.*;
import cpe.simulator.config.SimulatorConfig;
import cpe.simulator.core.IncidentEvolutionManager;
import cpe.simulator.core.IncidentGenerator;
import cpe.simulator.core.Simulator;
import cpe.simulator.domain.GeoZone;
import cpe.simulator.domain.PhaseType;
import cpe.simulator.domain.PhaseTypeCatalog;
import cpe.simulator.infrastructure.*;
import cpe.simulator.infrastructure.http.AuthStrategy;
import cpe.simulator.infrastructure.http.HttpApiClient;
import cpe.simulator.infrastructure.http.KeycloakAuthStrategy;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Factory pour construire le simulateur avec toutes ses dépendances. */
public final class SimulatorFactory {

  private SimulatorFactory() {}

  public static Simulator create(SimulatorConfig config, Logger logger)
      throws Exception {
    logger.info("Initialisation du simulateur...");

    // Clients HTTP
    HttpClient httpClient = createHttpClient(config);
    ObjectMapper mapper = createObjectMapper();
    AuthStrategy auth = createAuthStrategy(config, httpClient, mapper);
    HttpApiClient apiClient =
        new HttpApiClient(config.apiBaseUrl(), config.apiTimeoutMs(), auth, httpClient, mapper);

    // Services
    IncidentService incidentService = new SdmisIncidentService(apiClient);
    GeocodeService geocodeService = new SdmisGeocodeService(apiClient);

    // Chargement des données
    List<PhaseType> phaseTypes = incidentService.getPhaseTypes();
    PhaseTypeCatalog phaseCatalog = new PhaseTypeCatalog(phaseTypes);
    logger.info("Phase types chargés: " + phaseCatalog.size());

    Map<String, Double> probabilities = new ProbabilityLoader().load(config.probabilitiesPath());
    logger.info("Probabilités chargées: " + probabilities.size());

    GeoZone zone = new GeoZoneLoader().load(config.geoZonesPath(), config.geoZoneName());
    logger.info(
        "Zone géo: lat="
            + zone.latitudeMin()
            + ".."
            + zone.latitudeMax()
            + " lon="
            + zone.longitudeMin()
            + ".."
            + zone.longitudeMax());

    // Composants de simulation
    IncidentSelector selector = new ProbabilityBasedSelector(probabilities, config.rngSeed());
    DelayStrategy delayStrategy =
        new ExponentialDelayStrategy(config.incidentsPerHour(), config.rngSeed() + 2);

    IncidentGenerator generator =
        new IncidentGenerator(selector, phaseCatalog, zone, geocodeService, config.rngSeed() + 1);

    logger.info("Incidents par heure: " + config.incidentsPerHour());
    
    // Chargement des probabilités d'évolution des phases
    InputStream subProbStream = SimulatorFactory.class.getResourceAsStream(config.subProbabilitiesPath());
    if (subProbStream == null) {
      // Fallback: charge depuis le système de fichiers
      try {
        subProbStream = new java.io.FileInputStream(config.subProbabilitiesPath());
        logger.info("Chargement des probabilités d'évolution des phases depuis le système de fichiers: " + config.subProbabilitiesPath());
      } catch (Exception e) {
        throw new RuntimeException("Impossible de charger le fichier de probabilités d'évolution des phases: " + config.subProbabilitiesPath(), e);
      }
    }
    SubIncidentProbabilityLoader subIncidentProbabilityLoader = new SubIncidentProbabilityLoader(subProbStream);

    IncidentEvolutionManager evolutionManager = new IncidentEvolutionManager(subIncidentProbabilityLoader, config.rngSeed() + 3);
    return new Simulator(generator, incidentService, delayStrategy, logger, evolutionManager, phaseCatalog);
  }

  private static HttpClient createHttpClient(SimulatorConfig config) {
    long connectTimeout = Math.min(config.keycloakTimeoutMs(), config.apiTimeoutMs());
    return HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(connectTimeout))
        .version(HttpClient.Version.HTTP_1_1)
        .build();
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  private static AuthStrategy createAuthStrategy(
      SimulatorConfig config, HttpClient httpClient, ObjectMapper mapper) {
    return new KeycloakAuthStrategy(
        config.keycloakIssuer(),
        config.keycloakClientId(),
        config.keycloakClientSecret(),
        config.keycloakTimeoutMs(),
        config.keycloakTokenExpirySkewSeconds(),
        httpClient,
        mapper,
        Clock.systemUTC());
  }
}
