package cpe.simulator.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import cpe.simulator.api.IncidentService;
import cpe.simulator.domain.Incident;
import cpe.simulator.domain.PhaseType;
import cpe.simulator.infrastructure.http.HttpApiClient;
import java.io.IOException;
import java.util.List;

/** Implémentation du service d'incidents via l'API SDMIS. */
public final class SdmisIncidentService implements IncidentService {

  private static final TypeReference<List<PhaseType>> PHASE_TYPE_LIST = new TypeReference<>() {};

  private final HttpApiClient httpClient;

  public SdmisIncidentService(HttpApiClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public List<PhaseType> getPhaseTypes() throws IOException, InterruptedException {
    return httpClient.getList("/incidents/phase/types", PHASE_TYPE_LIST);
  }

  @Override
  public String createIncident(Incident incident) throws IOException, InterruptedException {
    IncidentRequest request = toRequest(incident);
    IncidentResponse response =
        httpClient.post("/qg/incidents/new", request, IncidentResponse.class);
    return response.incident != null ? response.incident.incidentId : null;
  }

  private IncidentRequest toRequest(Incident incident) {
    LocationDto location = null;
    if (incident.location() != null) {
      location =
          new LocationDto(
              incident.location().address(),
              incident.location().zipcode(),
              incident.location().city(),
              incident.location().latitude(),
              incident.location().longitude());
    }

    PhaseDto phase = new PhaseDto(incident.phaseTypeId(), incident.priority());

    return new IncidentRequest(
        location,
        incident.description(),
        incident.startedAt() != null ? incident.startedAt().toString() : null,
        phase);
  }

  // DTOs pour l'API
  private record IncidentRequest(
      LocationDto location,
      String description,
      @JsonProperty("incident_started_at") String incidentStartedAt,
      PhaseDto phase) {}

  private record LocationDto(
      String address, String zipcode, String city, Double latitude, Double longitude) {}

  private record PhaseDto(@JsonProperty("phase_type_id") String phaseTypeId, Integer priority) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record IncidentResponse(IncidentData incident) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record IncidentData(@JsonProperty("incident_id") String incidentId) {}
  }
}
