package cpe.simulator.infrastructure.http;

import java.net.http.HttpRequest;

/** Stratégie d'authentification pour les requêtes HTTP. */
public interface AuthStrategy {

  void apply(HttpRequest.Builder builder);
}
