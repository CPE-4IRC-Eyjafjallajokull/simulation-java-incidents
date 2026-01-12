package cpe.simulator.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Configuration du simulateur chargée depuis les variables d'environnement. */
public record SimulatorConfig(
    // Keycloak
    String keycloakIssuer,
    String keycloakClientId,
    String keycloakClientSecret,
    long keycloakTimeoutMs,
    long keycloakTokenExpirySkewSeconds,

    // API SDMIS
    String apiBaseUrl,
    long apiTimeoutMs,

    // Simulateur
    long rngSeed,
    String geoZonesPath,
    String geoZoneName,
    double incidentsPerHour,
    String probabilitiesPath,
    String subProbabilitiesPath) {
  public static SimulatorConfig fromEnvironment() {
    Map<String, String> env = loadEnv();

    return new SimulatorConfig(
        env.getOrDefault("KEYCLOAK_ISSUER", "http://localhost:8080/realms/sdmis"),
        requireEnv(env, "KEYCLOAK_CLIENT_ID"),
        requireEnv(env, "KEYCLOAK_CLIENT_SECRET"),
        parseLong(env, "KEYCLOAK_TIMEOUT_MS", 3_000L),
        parseLong(env, "KEYCLOAK_TOKEN_EXPIRY_SKEW_SECONDS", 30L),
        env.getOrDefault("SDMIS_API_BASE_URL", "http://localhost:3001"),
        parseLong(env, "SDMIS_API_TIMEOUT_MS", 5_000L),
        parseLong(env, "RNG_SEED", 42L),
        env.getOrDefault("GEO_ZONES_PATH", "geographic-zone.json"),
        env.getOrDefault("GEO_ZONE_NAME", "lyon_villeurbanne"),
        parseDouble(env, "INCIDENTS_PER_HOUR", 12.0),
        env.getOrDefault("INCIDENT_PROBABILITIES_PATH", "incident-probabilities.json"),
        env.getOrDefault("SUB_INCIDENT_PROBABILITIES_PATH", "sub-incident-probabilities.json"));
  }

  private static Map<String, String> loadEnv() {
    Map<String, String> merged = new HashMap<>();
    merged.putAll(loadDotenv());
    merged.putAll(System.getenv());
    return merged;
  }

  private static Map<String, String> loadDotenv() {
    Path dotenvPath = Path.of(".env");
    if (!Files.exists(dotenvPath)) {
      return Map.of();
    }
    Map<String, String> values = new HashMap<>();
    try {
      for (String line : Files.readAllLines(dotenvPath)) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }
        if (trimmed.startsWith("export ")) {
          trimmed = trimmed.substring(7).trim();
        }
        int sep = trimmed.indexOf('=');
        if (sep > 0) {
          String key = trimmed.substring(0, sep).trim();
          String value = trimmed.substring(sep + 1).trim();
          // Remove surrounding quotes
          if ((value.startsWith("\"") && value.endsWith("\""))
              || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
          }
          values.put(key, value);
        }
      }
    } catch (IOException ignored) {
    }
    return values;
  }

  private static String requireEnv(Map<String, String> env, String key) {
    String value = env.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required environment variable: " + key);
    }
    return value;
  }

  private static long parseLong(Map<String, String> env, String key, long defaultValue) {
    String value = env.get(key);

    if (value == null || value.isBlank()) {
      return defaultValue;
    }

    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private static double parseDouble(Map<String, String> env, String key, double defaultValue) {
    String value = env.get(key);

    if (value == null || value.isBlank()) {
      return defaultValue;
    }

    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
