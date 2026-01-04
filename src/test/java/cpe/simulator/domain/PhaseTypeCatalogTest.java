package cpe.simulator.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PhaseTypeCatalogTest {

  @Test
  void indexesByCode() {
    PhaseType pt1 = new PhaseType("id1", "cat1", "FIRE", "Fire", 3);
    PhaseType pt2 = new PhaseType("id2", "cat1", "SAP", "Medical", 2);
    PhaseTypeCatalog catalog = new PhaseTypeCatalog(List.of(pt1, pt2));

    assertEquals(2, catalog.size());
    assertEquals(pt1, catalog.byCode("FIRE"));
    assertEquals(pt2, catalog.byCode("SAP"));
  }

  @Test
  void returnsNullForUnknownCode() {
    PhaseTypeCatalog catalog =
        new PhaseTypeCatalog(List.of(new PhaseType("id1", "cat1", "FIRE", "Fire", 3)));

    assertNull(catalog.byCode("UNKNOWN"));
  }

  @Test
  void ignoresNullEntries() {
    PhaseType pt = new PhaseType("id1", "cat1", "FIRE", "Fire", 3);
    List<PhaseType> list = new java.util.ArrayList<>();
    list.add(pt);
    list.add(null);
    PhaseTypeCatalog catalog = new PhaseTypeCatalog(list);

    assertEquals(1, catalog.size());
  }

  @Test
  void ignoresEntriesWithNullCode() {
    PhaseType valid = new PhaseType("id1", "cat1", "FIRE", "Fire", 3);
    PhaseType invalid = new PhaseType("id2", "cat1", null, "No code", 1);
    PhaseTypeCatalog catalog = new PhaseTypeCatalog(List.of(valid, invalid));

    assertEquals(1, catalog.size());
  }

  @Test
  void returnsCodes() {
    PhaseTypeCatalog catalog =
        new PhaseTypeCatalog(
            List.of(
                new PhaseType("id1", "cat1", "FIRE", "Fire", 3),
                new PhaseType("id2", "cat1", "SAP", "Medical", 2)));

    assertTrue(catalog.codes().contains("FIRE"));
    assertTrue(catalog.codes().contains("SAP"));
  }
}
