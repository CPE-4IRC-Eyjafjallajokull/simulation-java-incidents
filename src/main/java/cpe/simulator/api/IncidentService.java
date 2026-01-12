package cpe.simulator.api;

import cpe.simulator.domain.Incident;
import cpe.simulator.domain.IncidentPhaseForApi;
import cpe.simulator.domain.PhaseType;
import java.io.IOException;
import java.util.List;

/** Service de gestion des incidents. */
public interface IncidentService {
  /** Ajoute une nouvelle phase à un incident existant. */
  void addIncidentPhase(String incidentId, IncidentPhaseForApi phase) throws IOException, InterruptedException;

  /** Récupère la liste des types de phases disponibles. */
  List<PhaseType> getPhaseTypes() throws IOException, InterruptedException;

  /**
   * Crée un nouvel incident.
   *
   * @param incident l'incident à créer
   * @return l'identifiant de l'incident créé
   */
  String createIncident(Incident incident) throws IOException, InterruptedException;
}
