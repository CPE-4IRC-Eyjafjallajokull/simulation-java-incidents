package cpe.simulator.loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import cpe.simulator.model.GeoZone;

public class GeoZoneLoader {

    public static GeoZone load(String path, String zoneName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        InputStream classpathStream = GeoZoneLoader.class.getClassLoader().getResourceAsStream(path);
        if (classpathStream != null) {
            root = mapper.readTree(classpathStream);
        } else {
            root = mapper.readTree(new File(path));
        }
        JsonNode zones = root.get("zones");
        if (zones == null || zones.get(zoneName) == null) {
            throw new IllegalArgumentException("Zone not found: " + zoneName);
        }
        JsonNode zoneNode = zones.get(zoneName);
        GeoZone zone = new GeoZone();
        zone.setLatitudeMin(zoneNode.get("latitude").get("min").asDouble());
        zone.setLatitudeMax(zoneNode.get("latitude").get("max").asDouble());
        zone.setLongitudeMin(zoneNode.get("longitude").get("min").asDouble());
        zone.setLongitudeMax(zoneNode.get("longitude").get("max").asDouble());
        return zone;
    }
}
