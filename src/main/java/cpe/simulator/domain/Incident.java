package cpe.simulator.domain;

import java.time.OffsetDateTime;

/** Incident à créer. */
public record Incident(
  String code,
  String label,
  String description,
  Location location,
  String phaseTypeId,
  int priority,
  OffsetDateTime startedAt,
  java.util.List<IncidentPhase> phases,
  IncidentPhase currentPhase) {
  /** Retourne une nouvelle instance d'Incident avec la phase courante modifiée. */
  public Incident withCurrentPhase(IncidentPhase newCurrentPhase) {
    return new Incident(code, label, description, location, phaseTypeId, priority, startedAt, phases, newCurrentPhase);
  }
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String code;
    private String label;
    private String description;
    private Location location;
    private String phaseTypeId;
    private int priority;
    private OffsetDateTime startedAt;
    private java.util.List<IncidentPhase> phases = new java.util.ArrayList<>();
    private IncidentPhase currentPhase;

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder label(String label) {
      this.label = label;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public Builder phaseType(PhaseType phaseType) {
      this.phaseTypeId = phaseType.phaseTypeId();
      this.priority = phaseType.defaultCriticity();
      return this;
    }

    public Builder priority(int priority) {
      this.priority = priority;
      return this;
    }

    public Builder startedAt(OffsetDateTime startedAt) {
      this.startedAt = startedAt;
      return this;
    }

    public Builder phases(java.util.List<IncidentPhase> phases) {
      this.phases = phases;
      return this;
    }

    public Builder currentPhase(IncidentPhase currentPhase) {
      this.currentPhase = currentPhase;
      return this;
    }

    public Incident build() {
      return new Incident(
          code, label, description, location,
          phaseTypeId, priority, startedAt, phases, currentPhase);
    }
  }
}
