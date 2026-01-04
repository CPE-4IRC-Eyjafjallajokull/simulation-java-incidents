package cpe.simulator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Type de phase d'incident. */
public record PhaseType(
    @JsonProperty("phase_type_id") String phaseTypeId,
    @JsonProperty("phase_category_id") String phaseCategoryId,
    String code,
    String label,
    @JsonProperty("default_criticity") int defaultCriticity) {}
