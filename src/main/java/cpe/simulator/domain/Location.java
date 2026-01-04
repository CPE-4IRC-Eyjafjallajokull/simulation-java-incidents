package cpe.simulator.domain;

/** Localisation d'un incident. */
public record Location(
    String address, String zipcode, String city, double latitude, double longitude) {
  /** Crée une location avec des valeurs par défaut pour l'adresse. */
  public static Location ofCoordinates(double latitude, double longitude) {
    return new Location("Adresse simulée", "69000", "Lyon", latitude, longitude);
  }

  /** Crée une copie avec les informations d'adresse mises à jour. */
  public Location withAddress(String address, String zipcode, String city) {
    return new Location(
        address != null ? address : this.address,
        zipcode != null ? zipcode : this.zipcode,
        city != null ? city : this.city,
        this.latitude,
        this.longitude);
  }
}
