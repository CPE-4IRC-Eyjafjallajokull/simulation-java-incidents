package cpe.simulator.config;

public class SimulatorConfig {

    public static final String API_BASE_URL =
            System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8080");

    public static final String API_TOKEN =
            System.getenv("API_TOKEN");

    public static final long RNG_SEED =
            Long.parseLong(System.getenv().getOrDefault("RNG_SEED", "42"));
}
