package cpe.simulator.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import cpe.simulator.domain.GeoZone;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class GeoZoneLoaderTest {

  @Test
  void loadsZoneFromClasspath() throws IOException {
    GeoZoneLoader loader = new GeoZoneLoader();
    GeoZone zone = loader.load("geographic-zone.json", "lyon_1");

    assertEquals(45.764, zone.latitudeMin());
    assertEquals(45.775, zone.latitudeMax());
    assertEquals(4.828, zone.longitudeMin());
    assertEquals(4.838, zone.longitudeMax());
  }

  @Test
  void loadsAllZones() throws IOException {
    GeoZoneLoader loader = new GeoZoneLoader();
    List<GeoZone> zones = loader.loadAll("geographic-zone.json");

    assertEquals(10, zones.size()); // lyon_1 à lyon_9 + villeurbanne
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
