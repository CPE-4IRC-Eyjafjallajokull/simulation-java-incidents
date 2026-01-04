package cpe.simulator.model;

import java.time.LocalDateTime;

// Modèle représentant un incident avec un code et une date d'occurrence

public class Incident {

    private String code;
    private LocalDateTime occurredAt;

    public Incident(String code) {
        this.code = code;
        this.occurredAt = LocalDateTime.now();
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
