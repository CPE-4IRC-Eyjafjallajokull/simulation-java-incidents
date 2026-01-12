package cpe.simulator.domain;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Zone restreinte (parc, etc.) où certains incidents sont interdits. */
public record RestrictedZone(
    String type,
    String name,
    Polygon polygon,
    Set<String> forbiddenIncidents
) {
    public RestrictedZone(String type, String name, Polygon polygon, List<String> forbiddenIncidents) {
        this(type, name, polygon, new HashSet<>(forbiddenIncidents));
    }

    /** Vérifie si un point est dans cette zone restreinte. */
    public boolean contains(double latitude, double longitude) {
        return polygon.contains(latitude, longitude);
    }

    /** Vérifie si un incident est interdit dans cette zone. */
    public boolean isIncidentForbidden(String incidentCode) {
        return forbiddenIncidents.contains(incidentCode);
    }
}
