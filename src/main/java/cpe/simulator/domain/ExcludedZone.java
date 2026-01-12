package cpe.simulator.domain;

/** Zone exclue (eau, etc.) où aucun incident ne peut se produire. */
public record ExcludedZone(
    String type,
    String name,
    Polygon polygon
) {
    /** Vérifie si un point est dans cette zone exclue. */
    public boolean contains(double latitude, double longitude) {
        return polygon.contains(latitude, longitude);
    }
}
