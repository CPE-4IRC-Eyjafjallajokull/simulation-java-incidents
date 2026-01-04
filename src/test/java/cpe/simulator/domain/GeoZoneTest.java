package cpe.simulator.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

class GeoZoneTest {

  @Test
  void createsValidGeoZone() {
    GeoZone zone = new GeoZone(45.74, 45.79, 4.82, 4.90);

    assertEquals(45.74, zone.latitudeMin());
    assertEquals(45.79, zone.latitudeMax());
    assertEquals(4.82, zone.longitudeMin());
    assertEquals(4.90, zone.longitudeMax());
  }

  @Test
  void rejectsInvalidLatitudeRange() {
    assertThrows(IllegalArgumentException.class, () -> new GeoZone(45.79, 45.74, 4.82, 4.90));
  }

  @Test
  void rejectsInvalidLongitudeRange() {
    assertThrows(IllegalArgumentException.class, () -> new GeoZone(45.74, 45.79, 4.90, 4.82));
  }

  @Test
  void generatesRandomLatitudeInRange() {
    GeoZone zone = new GeoZone(45.74, 45.79, 4.82, 4.90);
    Random rng = new Random(42);

    for (int i = 0; i < 100; i++) {
      double lat = zone.randomLatitude(rng);
      assertTrue(lat >= 45.74 && lat <= 45.79);
    }
  }

  @Test
  void generatesRandomLongitudeInRange() {
    GeoZone zone = new GeoZone(45.74, 45.79, 4.82, 4.90);
    Random rng = new Random(42);

    for (int i = 0; i < 100; i++) {
      double lon = zone.randomLongitude(rng);
      assertTrue(lon >= 4.82 && lon <= 4.90);
    }
  }
}
