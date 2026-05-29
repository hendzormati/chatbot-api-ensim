# ChatBot Assistance Personnelle  ENSIM Interoperabilite

Un chatbot assistant personnel developpe avec Spring Boot, exposant une API REST et interagissant avec Telegram via un systeme de polling. Le bot repond a des commandes en langage naturel et s'integre a trois API externes : l'API Bot Telegram, OpenWeatherMap et OMDB.

---

## Table des matieres

- [Structure du projet](#structure-du-projet)
- [Prerequis](#prerequis)
- [Configuration](#configuration)
- [Lancement de l'application](#lancement-de-lapplication)
- [API REST](#api-rest)
    - [Messages](#messages)
    - [Blagues](#blagues)
    - [Meteo](#meteo)
    - [Films](#films)
- [Commandes du chatbot Telegram](#commandes-du-chatbot-telegram)
    - [Blagues](#blagues-1)
    - [Meteo](#meteo-1)
    - [Films](#films-1)
- [Authentification](#authentification)
- [Specification OpenAPI](#specification-openapi)
---

## Prerequis

- Java 22
- Maven 3.8 ou superieur
- Un compte Telegram et un bot cree via BotFather
- Un compte OpenWeatherMap (offre gratuite)
- Un compte OMDB (offre gratuite, 1000 requetes par jour)

---

## Configuration

Renseigner les valeurs suivantes dans `src/main/resources/application.properties` :

```properties
# Serveur
server.port=9090

# Telegram
telegram.bot.token=VOTRE_TOKEN_BOT
telegram.api.base-url=https://api.telegram.org/bot
telegram.bot.id=VOTRE_CHAT_ID

# OpenWeatherMap
openweathermap.api.key=VOTRE_CLE_OPENWEATHER

# OMDB
omdb.api.key=VOTRE_CLE_OMDB
omdb.api.base-url=http://www.omdbapi.com/

# Securite API
api.security.token=VOTRE_TOKEN_CHOISI
```

Comment obtenir chaque cle :

- **Token Telegram** : envoyer un message a @BotFather sur Telegram, executer /newbot et copier le token fourni.
- **Chat ID Telegram** : envoyer n'importe quel message a votre bot, puis ouvrir `https://api.telegram.org/botVOTRE_TOKEN/getUpdates` dans un navigateur et chercher `"chat":{"id": ...}`.
- **Cle OpenWeatherMap** : creer un compte gratuit sur openweathermap.org, aller dans la section API keys.
- **Cle OMDB** : s'inscrire sur omdbapi.com/apikey.aspx, choisir l'offre gratuite, confirmer via le lien recu par email.

---

## Lancement de l'application

```bash
mvn clean compile
mvn spring-boot:run
```

L'interface Swagger UI est accessible a : `http://localhost:9090/swagger-ui/index.html`

Le polling Telegram demarre automatiquement au lancement. Le bot interroge l'API Telegram toutes les 3 secondes pour detecter les nouveaux messages.

---

## API REST

Tous les endpoints necessitent le header `X-API-KEY: VOTRE_TOKEN_CHOISI`.

URL de base : `http://localhost:9090`

---

### Messages

#### Envoyer un message sur une conversation Telegram

```
POST /api/message
```

Corps de la requete :
```json
{
  "chatId": "123456789",
  "text": "Bonjour depuis l'API"
}
```

Reponse :
```json
{
  "success": true,
  "chatId": "123456789",
  "messageId": 42
}
```

#### Envoyer plusieurs messages en une seule requete

```
POST /api/messages
```

Corps de la requete :
```json
{
  "chatId": "123456789",
  "texts": ["Premier message", "Deuxieme message", "Troisieme message"]
}
```

Reponse : `"3 message(s) envoye(s) !"`

---

### Blagues

#### Obtenir toutes les blagues

```
GET /api/jokes
```

#### Obtenir une blague aleatoire

```
GET /api/jokes/random
```

#### Obtenir la blague la mieux notee

```
GET /api/jokes/best
```

#### Obtenir la blague la moins bien notee

```
GET /api/jokes/worst
```

#### Obtenir une blague par son identifiant

```
GET /api/jokes/{id}
```

#### Rechercher des blagues par mot-cle dans le titre

```
GET /api/jokes/search?titre=programmeur
```

#### Ajouter une nouvelle blague

```
POST /api/jokes
```

Corps de la requete :
```json
{
  "title": "Le chat",
  "text": "Pourquoi le chat est tombe du toit ? Parce qu'il etait chat-percute !",
  "rating": 7.0
}
```

#### Modifier une blague existante

```
PUT /api/jokes/{id}
```

Corps de la requete :
```json
{
  "title": "Titre modifie",
  "text": "Texte modifie",
  "rating": 8.5
}
```

#### Noter une blague sur 10

```
PATCH /api/jokes/{id}/rate
```

Corps de la requete :
```json
{
  "rating": 9.0
}
```

#### Supprimer une blague

```
DELETE /api/jokes/{id}
```

Reponse : HTTP 204 No Content

---

### Meteo

#### Obtenir la meteo du jour pour une ville

```
GET /api/meteo?ville=Paris
```

Reponse :
```json
{
  "ville": "Paris",
  "meteo": "Clouds",
  "details": "ciel nuageux",
  "temperature": 18.5
}
```

#### Obtenir la meteo du jour et les previsions sur 2 jours

```
GET /api/meteo/forecast?ville=Paris
```

Reponse :
```json
{
  "ville": "Paris",
  "today": {
    "meteo": "Clouds",
    "details": "ciel nuageux",
    "temperature": 18.5
  },
  "forecast": [
    {
      "date": "2025-03-16",
      "meteo": "Rain",
      "details": "legere pluie",
      "temperatureMatin": 12.0,
      "temperatureApresMidi": 16.5
    },
    {
      "date": "2025-03-17",
      "meteo": "Clear",
      "details": "ciel degager",
      "temperatureMatin": 14.0,
      "temperatureApresMidi": 21.0
    }
  ]
}
```

---

### Films

#### Rechercher un film par titre

```
GET /api/film?titre=Inception
```

Reponse :
```json
{
  "titre": "Inception",
  "synopsis": "Un voleur qui s'infiltre dans les reves de ses cibles pour leur derober des secrets...",
  "note": 8.8,
  "annee": "2010",
  "affiche": "https://m.media-amazon.com/images/..."
}
```

---

## Commandes du chatbot Telegram

Le bot ecoute les messages et repond automatiquement. Toutes les reponses citent le message d'origine (reply).

### Blagues

| Ce que vous ecrivez                | Ce que le bot fait             |
|------------------------------------|--------------------------------|
| `blague`                           | Retourne une blague aleatoire avec sa note |
| `bonne blague`                     | Retourne la blague la mieux notee |
| `blague nulle`                     | Retourne la blague la moins bien notee |
| `voir blague 3`                    | Retourne la blague dont l'identifiant est 3 |
| `titre blague programmeur`         | Cherche les blagues contenant "programmeur" dans le titre |
| `noter blague 3 note 8`            | Attribue la note 8 a la blague dont l'identifiant est 3 |
| `ajouter blague Chat Pourquoi.. 7` | Cree une nouvelle blague |
| `modifier blague 3 Nouveau titre Nouveau texte  9` | Modifie la blague dont l'identifiant est 3 |
| `supprimer blague 3`               | Supprime la blague dont l'identifiant est 3 |

Pour les commandes `ajouter` et `modifier`, les champs sont separes par le caractere `|`.

### Meteo

| Ce que vous ecrivez | Ce que le bot fait |
|---|---|
| `meteo Paris` | Retourne la meteo du jour a Paris |
| `meteo` | Retourne la meteo du jour a Paris (ville par defaut) |
| `previsions Lyon` | Retourne la meteo du jour et les 2 prochains jours pour Lyon |

### Films

| Ce que vous ecrivez | Ce que le bot fait |
|---|---|
| `film Inception` | Retourne l'affiche, le synopsis, la note IMDB et l'annee de sortie |
| `film The Dark Knight` | Fonctionne avec les titres complets incluant des espaces |

Si une affiche est disponible, elle est envoyee sous forme d'image avec le synopsis en legende. Sinon, les informations sont envoyees sous forme de texte.

### Commande non reconnue

Si le bot ne reconnait pas une commande, il repond avec la liste complete des commandes disponibles.

---

## Authentification

Tous les endpoints de l'API REST sont proteges par une cle API transmise dans un header HTTP.

Nom du header : `X-API-KEY`

Exemple avec curl :
```bash
curl -H "X-API-KEY: votre-token" http://localhost:9090/api/jokes/random
```

Exemple avec Postman : ajouter un header `X-API-KEY` avec la valeur de votre token dans l'onglet Headers de chaque requete.

Le chatbot Telegram ne necessite pas d'authentification  il repond a toute personne qui envoie un message a votre bot.

---

## Specification OpenAPI

La specification complete se trouve dans `src/main/resources/openapi.yaml`.

Elle est egalement servie dynamiquement par l'application en cours d'execution a l'adresse :
```
http://localhost:9090/v3/api-docs
```

Et visualisable via Swagger UI a :
```
http://localhost:9090/swagger-ui/index.html
```

Les classes de modeles et les interfaces API situees dans `fr/ensim/interop/introrest/api/` et `fr/ensim/interop/introrest/model/generated/` sont auto-generees depuis ce fichier a la compilation grace au plugin Maven `openapi-generator-maven-plugin`. Ces fichiers ne doivent pas etre modifies manuellement.