package cpe.simulator.domain;

/** Représente une phase d'évolution d'un incident pour l'API (POST /incidents/phases). */
public record IncidentPhaseForApi(
    String phaseTypeId,
    int priority,
    java.time.OffsetDateTime startedAt,
    java.time.OffsetDateTime endedAt
) {}
