package cpe.simulator.domain;

import java.util.List;

/** Polygone défini par une liste de points (latitude, longitude). */
public record Polygon(List<double[]> points) {

    /**
     * Vérifie si un point est à l'intérieur du polygone.
     * Utilise l'algorithme du ray-casting.
     */
    public boolean contains(double latitude, double longitude) {
        if (points == null || points.size() < 3) {
            return false;
        }

        boolean inside = false;
        int n = points.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double[] pi = points.get(i);
            double[] pj = points.get(j);

            double yi = pi[0], xi = pi[1];
            double yj = pj[0], xj = pj[1];

            if (((yi > latitude) != (yj > latitude))
                && (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }

        return inside;
    }
}
