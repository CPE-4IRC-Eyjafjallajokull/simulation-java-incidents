package cpe.simulator.core;

import static org.junit.jupiter.api.Assertions.*;

import cpe.simulator.api.GeocodeService;
import cpe.simulator.api.IncidentSelector;
import cpe.simulator.domain.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncidentGeneratorTest {

  private PhaseTypeCatalog catalog;
  private GeoZone zone;
  private IncidentSelector selector;

  @BeforeEach
  void setUp() {
    catalog =
        new PhaseTypeCatalog(
            List.of(
                new PhaseType("fire-id", "cat1", "FIRE", "Fire", 3),
                new PhaseType("sap-id", "cat1", "SAP", "Medical", 2)));
    zone = new GeoZone(45.74, 45.79, 4.82, 4.90);
    selector = () -> "FIRE"; // Toujours retourne FIRE
  }

  @Test
  void generatesIncidentWithValidCode() {
    IncidentGenerator generator = new IncidentGenerator(selector, catalog, zone, null, 42L);

    Optional<Incident> result = generator.generate();

    assertTrue(result.isPresent());
    Incident incident = result.get();
    assertEquals("FIRE", incident.code());
    assertEquals("fire-id", incident.phaseTypeId());
    assertEquals(3, incident.priority());
    assertNotNull(incident.location());
    assertNotNull(incident.startedAt());
  }

  @Test
  void returnsEmptyForUnknownCode() {
    IncidentSelector unknownSelector = () -> "UNKNOWN";
    IncidentGenerator generator = new IncidentGenerator(unknownSelector, catalog, zone, null, 42L);

    Optional<Incident> result = generator.generate();

    assertTrue(result.isEmpty());
  }

  @Test
  void locationIsWithinGeoZone() {
    IncidentGenerator generator = new IncidentGenerator(selector, catalog, zone, null, 42L);

    for (int i = 0; i < 10; i++) {
      Optional<Incident> result = generator.generate();
      assertTrue(result.isPresent());
      Location loc = result.get().location();
      assertTrue(loc.latitude() >= 45.74 && loc.latitude() <= 45.79);
      assertTrue(loc.longitude() >= 4.82 && loc.longitude() <= 4.90);
    }
  }

  @Test
  void enrichesLocationWithGeocode() {
    GeocodeService geocode =
        (lat, lon) -> Optional.of(new Location("10 Rue Test", "69001", "Lyon", lat, lon));
    IncidentGenerator generator = new IncidentGenerator(selector, catalog, zone, geocode, 42L);

    Optional<Incident> result = generator.generate();

    assertTrue(result.isPresent());
    assertEquals("10 Rue Test", result.get().location().address());
    assertEquals("69001", result.get().location().zipcode());
    assertEquals("Lyon", result.get().location().city());
  }
}
