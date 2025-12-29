package cpe.simulator.model;

import java.time.LocalDateTime;

// Modèle représentant une requête de création de phase d'incident

public class IncidentPhaseCreateRequest {
    private String incidentId;
    private String phaseTypeId;
    private int priority;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getPhaseTypeId() {
        return phaseTypeId;
    }

    public void setPhaseTypeId(String phaseTypeId) {
        this.phaseTypeId = phaseTypeId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
}
