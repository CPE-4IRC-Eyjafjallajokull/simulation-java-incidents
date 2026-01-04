package cpe.simulator.api;

/** Sélectionne un code d'incident selon une stratégie donnée. */
public interface IncidentSelector {

  /**
   * @return le code d'incident sélectionné
   */
  String pickIncidentCode();
}
