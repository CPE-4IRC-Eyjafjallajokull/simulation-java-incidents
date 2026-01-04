package cpe.simulator.api;

import cpe.simulator.domain.Location;
import java.io.IOException;
import java.util.Optional;

/** Service de géocodage inverse. */
public interface GeocodeService {

  /**
   * Résout une adresse à partir de coordonnées GPS.
   *
   * @param latitude latitude
   * @param longitude longitude
   * @return l'adresse résolue, ou vide si non trouvée
   */
  Optional<Location> reverseGeocode(double latitude, double longitude)
      throws IOException, InterruptedException;
}
