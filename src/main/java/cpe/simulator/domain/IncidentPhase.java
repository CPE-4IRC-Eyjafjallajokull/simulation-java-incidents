package cpe.simulator.domain;

import java.time.OffsetDateTime;

/** Représente une phase d'évolution d'un incident. */
public record IncidentPhase(
    String code, // code d'incident (ex: SAP_CARDIAC_ARREST)
    String label, // label ou nom de la phase
    OffsetDateTime timestamp // date/heure de début de la phase
) {}
