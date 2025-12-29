package cpe.simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

// Modèle représentant la réponse de création d'un incident

public class IncidentCreateResponse {
    @JsonProperty("incident_id")
    private String incidentId;

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }
}
