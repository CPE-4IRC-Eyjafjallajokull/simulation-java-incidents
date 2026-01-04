package cpe.simulator.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import cpe.simulator.domain.GeoZone;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GeoZoneLoaderTest {

  @Test
  void loadsZoneFromClasspath() throws IOException {
    GeoZoneLoader loader = new GeoZoneLoader();
    GeoZone zone = loader.load("geographic-zone.json", "lyon_villeurbanne");

    assertEquals(45.74, zone.latitudeMin());
    assertEquals(45.79, zone.latitudeMax());
    assertEquals(4.82, zone.longitudeMin());
    assertEquals(4.90, zone.longitudeMax());
  }

  @Test
  void throwsForUnknownZone() {
    GeoZoneLoader loader = new GeoZoneLoader();

    assertThrows(
        IllegalArgumentException.class, () -> loader.load("geographic-zone.json", "unknown_zone"));
  }

  @Test
  void throwsForMissingFile() {
    GeoZoneLoader loader = new GeoZoneLoader();

    assertThrows(IOException.class, () -> loader.load("nonexistent.json", "lyon"));
  }
}
