package cpe.simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

// Modèle représentant un type de phase d'incident

public class PhaseType {
    @JsonProperty("phase_category_id")
    private String phaseCategoryId;
    private String code;
    private String label;
    @JsonProperty("default_criticity")
    private int defaultCriticity;
    @JsonProperty("phase_type_id")
    private String phaseTypeId;

    public String getPhaseCategoryId() {
        return phaseCategoryId;
    }

    public void setPhaseCategoryId(String phaseCategoryId) {
        this.phaseCategoryId = phaseCategoryId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getDefaultCriticity() {
        return defaultCriticity;
    }

    public void setDefaultCriticity(int defaultCriticity) {
        this.defaultCriticity = defaultCriticity;
    }

    public String getPhaseTypeId() {
        return phaseTypeId;
    }

    public void setPhaseTypeId(String phaseTypeId) {
        this.phaseTypeId = phaseTypeId;
    }
}
