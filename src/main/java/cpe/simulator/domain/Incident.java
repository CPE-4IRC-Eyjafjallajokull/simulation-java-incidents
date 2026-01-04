package cpe.simulator.domain;

import java.time.OffsetDateTime;

/** Incident à créer. */
public record Incident(
    String code,
    String description,
    Location location,
    String phaseTypeId,
    int priority,
    OffsetDateTime startedAt) {
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String code;
    private String description;
    private Location location;
    private String phaseTypeId;
    private int priority;
    private OffsetDateTime startedAt;

    public Builder code(String code) {
      this.code = code;
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

    public Builder startedAt(OffsetDateTime startedAt) {
      this.startedAt = startedAt;
      return this;
    }

    public Incident build() {
      return new Incident(code, description, location, phaseTypeId, priority, startedAt);
    }
  }
}
