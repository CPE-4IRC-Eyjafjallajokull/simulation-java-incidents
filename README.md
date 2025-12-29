# Simulation Java Incidents

Simulateur d'incidents pour alimenter le QG en environnement de test. Il sélectionne un code d'incident selon des probabilités, envoie un incident à l'API, puis crée la phase associée.

## Fonctionnement
- Charge les probabilités depuis `src/main/resources/incident-probabilities.json` (code `000` est ignoré).
- Tire un code avec un RNG créé à partir d'une seed (`RNG_SEED`) pour des runs reproductibles.
- Récupère la liste des phase types via `GET /incidents/phase/types`.
- Crée un incident via `POST /incidents` en ajoutant des coordonnées aléatoires dans la zone géographique configurée et en option l'adresse (reverse geocoding).
- Crée la phase associée via `POST /incident/phase` en réutilisant l`incident_id` retourné.

## Configuration (variables d'environnement)
- `API_BASE_URL` (ex: `https://api.example.com`)
- `API_TOKEN` (secret, bearer utilisé pour tous les appels)
- `OPERATOR_ID` (auteur de l'incident, défaut `operator-demo`)
- `RNG_SEED` (entier 64 bits, défaut `42`)
- `GEO_ZONES_PATH` (chemin ou ressource classpath vers le JSON des zones, défaut `geographic-zone.json`)
- `GEO_ZONE_NAME` (nom de la zone à utiliser, défaut `lyon_villeurbanne`)
- `ENABLE_REVERSE_GEOCODE` (`true`/`false`, défaut `true`)
- `REVERSE_GEOCODE_URL` (défaut `https://api-adresse.data.gouv.fr/reverse/?`)

## Fichiers de données
- Probabilités : `src/main/resources/incident-probabilities.json`
- Zones géographiques : `src/main/resources/geographic-zone.json` (lat/lon min/max par zone)

## Build et tests
```bash
mvn test          # exécute la suite de tests
mvn clean package # génère le jar dans target/
```

## Exécution (exemple)
```bash
API_BASE_URL=https://api.example.com \
API_TOKEN=xxx \
OPERATOR_ID=operator-demo \
GEO_ZONES_PATH=src/main/resources/geographic-zone.json \
GEO_ZONE_NAME=lyon_villeurbanne \
mvn -q -DskipTests exec:java \
  -Dexec.mainClass=cpe.simulator.SimulatorApp \
  -Dexec.classpathScope=runtime
```

## Appels API utilisés
- `GET /incidents/phase/types` : liste des phase types (code, phase_type_id, criticité par défaut)
- `POST /incidents` : création d'incident, retourne `incident_id`
- `POST /incident/phase` : création de la phase liée à l'incident

## Notes
- L'adresse est enrichie via reverse geocoding (timeout court, fallback sur valeurs simulées si l'appel échoue).
- Le RNG est semé pour faciliter les reproductions; changez `RNG_SEED` pour varier les scénarios.
