package cpe.simulator.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class IncidentTest {

  @Test
  void buildsIncidentWithAllFields() {
    PhaseType phaseType = new PhaseType("pt-123", "cat1", "FIRE", "Fire", 3);
    Location location = Location.ofCoordinates(45.75, 4.85);
    OffsetDateTime now = OffsetDateTime.now();

    Incident incident =
        Incident.builder()
            .code("FIRE")
            .description("Test incident")
            .location(location)
            .phaseType(phaseType)
            .startedAt(now)
            .build();

    assertEquals("FIRE", incident.code());
    assertEquals("Test incident", incident.description());
    assertEquals(location, incident.location());
    assertEquals("pt-123", incident.phaseTypeId());
    assertEquals(3, incident.priority());
    assertEquals(now, incident.startedAt());
  }

  @Test
  void buildsIncidentWithMinimalFields() {
    Incident incident = Incident.builder().code("TEST").build();

    assertEquals("TEST", incident.code());
    assertNull(incident.description());
    assertNull(incident.location());
  }
}
