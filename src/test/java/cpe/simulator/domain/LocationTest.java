package cpe.simulator.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LocationTest {

  @Test
  void createsLocationFromCoordinates() {
    Location loc = Location.ofCoordinates(45.75, 4.85);

    assertEquals(45.75, loc.latitude());
    assertEquals(4.85, loc.longitude());
    assertNotNull(loc.address());
    assertNotNull(loc.zipcode());
    assertNotNull(loc.city());
  }

  @Test
  void updatesAddressInfo() {
    Location base = Location.ofCoordinates(45.75, 4.85);
    Location updated = base.withAddress("10 Rue de la Paix", "69001", "Lyon");

    assertEquals("10 Rue de la Paix", updated.address());
    assertEquals("69001", updated.zipcode());
    assertEquals("Lyon", updated.city());
    assertEquals(base.latitude(), updated.latitude());
    assertEquals(base.longitude(), updated.longitude());
  }

  @Test
  void preservesOriginalValuesWhenNull() {
    Location base = new Location("Original", "00000", "Original City", 45.75, 4.85);
    Location updated = base.withAddress(null, null, null);

    assertEquals("Original", updated.address());
    assertEquals("00000", updated.zipcode());
    assertEquals("Original City", updated.city());
  }
}
