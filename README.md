# Simulation Java Incidents

Simulateur d'incidents pour alimenter le QG en environnement de test. Il sélectionne un code d'incident selon des probabilités, envoie un incident à l'API, puis boucle pour simuler des créations en continu.

## Architecture

Ce projet suit les principes **SOLID** et une architecture en couches :

### Principes appliqués
- **Single Responsibility** : chaque classe a une responsabilité unique
- **Open/Closed** : extensible via interfaces (Logger, DelayStrategy, IncidentSelector)
- **Liskov Substitution** : les implémentations sont interchangeables
- **Interface Segregation** : interfaces focalisées (5 interfaces métier)
- **Dependency Inversion** : dépendances via interfaces, injection via factory
- **Immutabilité** : tous les objets du domaine sont des records Java 17
- **Testabilité** : 34 tests unitaires couvrant la logique métier

## Fonctionnement
- Charge les probabilités depuis `src/main/resources/incident-probabilities.json` (code `NO_INCIDENT` est ignoré).
- Tire un code avec un RNG créé à partir d'une seed (`RNG_SEED`) pour des runs reproductibles.
- Récupère la liste des phase types via `GET /incidents/phase/types`.
- Valide que toutes les phases de l'API sont présentes dans les probabilités (hors `NO_INCIDENT`).
- Crée un incident via `POST /qg/incidents/new` en ajoutant des coordonnées aléatoires dans la zone géographique configurée et enrichit l'adresse via `/geocode/reverse`.
- Répète en continu avec un délai aléatoire basé sur `INCIDENTS_PER_HOUR`.

## Configuration (variables d'environnement)
Le fichier `.env` à la racine du projet est chargé automatiquement (si présent). Les variables d'environnement du shell restent prioritaires.

### Variables SDMIS API
- `SDMIS_API_BASE_URL` (ex: `https://api.example.com`)
- `SDMIS_API_TIMEOUT_MS` (défaut `5000`)

### Variables Keycloak
- `KEYCLOAK_ISSUER` (ex: `https://keycloak.example.com/realms/sdmis`)
- `KEYCLOAK_CLIENT_ID`
- `KEYCLOAK_CLIENT_SECRET`
- `KEYCLOAK_TIMEOUT_MS` (défaut `3000`)
- `KEYCLOAK_TOKEN_EXPIRY_SKEW_SECONDS` (défaut `30`)

### Variables de simulation
- `RNG_SEED` (entier 64 bits, défaut `42`)
- `GEO_ZONES_PATH` (chemin ou ressource classpath vers le JSON des zones, défaut `geographic-zone.json`)
- `GEO_ZONE_NAME` (nom de la zone à utiliser, défaut `lyon_villeurbanne`)
- `INCIDENTS_PER_HOUR` (taux moyen d'incidents par heure, défaut `12`)
- `INCIDENT_PROBABILITIES_PATH` (chemin ou ressource classpath des probabilités, défaut `incident-probabilities.json`)

## Fichiers de données
- Probabilités : `src/main/resources/incident-probabilities.json` (surcharge possible via `INCIDENT_PROBABILITIES_PATH`)
- Zones géographiques : `src/main/resources/geographic-zone.json` (lat/lon min/max par zone)

## Build et tests
```bash
mvn test          # exécute la suite de 34 tests unitaires
mvn clean package # génère le jar dans target/ (inclut les tests)
```

## Exécution

### Avec Maven
```bash
java -jar target/simulateur_java_incidents-1.0-SNAPSHOT.jar
```

### Avec variables d'environnement inline
```bash
SDMIS_API_BASE_URL=https://api.example.com \
KEYCLOAK_ISSUER=https://keycloak.example.com/realms/sdmis \
KEYCLOAK_CLIENT_ID=client-id \
KEYCLOAK_CLIENT_SECRET=client-secret \
GEO_ZONE_NAME=lyon_villeurbanne \
java -jar target/simulateur_java_incidents-1.0-SNAPSHOT.jar
```

### Avec fichier .env
Créez un fichier `.env` à la racine du projet :
```env
SDMIS_API_BASE_URL=https://api.example.com
KEYCLOAK_ISSUER=https://keycloak.example.com/realms/sdmis
KEYCLOAK_CLIENT_ID=client-id
KEYCLOAK_CLIENT_SECRET=client-secret
GEO_ZONE_NAME=lyon_villeurbanne
INCIDENTS_PER_HOUR=12
```

Puis lancez simplement :
```bash
java -jar target/simulateur_java_incidents-1.0-SNAPSHOT.jar
```

## Appels API utilisés
- `GET /incidents/phase/types` : liste des phase types (code, phase_type_id, criticité par défaut)
- `POST /qg/incidents/new` : création d'incident (et phase initiale) via un seul appel
- `GET /geocode/reverse` : enrichissement de l'adresse à partir des coordonnées

## Notes
- L'adresse est enrichie via l'API SDMIS `/geocode/reverse` (fallback sur valeurs simulées si l'appel échoue).
- Le RNG est semé pour faciliter les reproductions; changez `RNG_SEED` pour varier les scénarios.
