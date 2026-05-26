# ENSAM Bibliothèque API

Projet backend Spring Boot 3.2+ pour la gestion d'une bibliothèque. Le projet utilise une architecture en couches stricte, JWT pour la sécurité, pagination, filtrage par spécifications, export PDF/XLSX, et Swagger.

## Structure du projet

- `src/main/java/com/ensam/projet` : code source Java
- `src/main/resources` : configuration et données d'initialisation
- `src/test/java/com/ensam/projet` : tests unitaires et d'intégration

## Prérequis

- Java 17
- Maven 3.8+
- MySQL local ou conteneur Docker

## Lancer le projet

1. Construire le projet :
   ```bash
   mvn clean package
   ```
2. Lancer l'application :
   ```bash
   mvn spring-boot:run
   ```
3. L'API est disponible sur `http://localhost:8080`

## Swagger

- `http://localhost:8080/swagger-ui.html`

## Base de données

La configuration par défaut utilise MySQL :

- URL : `jdbc:mysql://localhost:3306/ensam_bibliotheque?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC`
- Username : `root`
- Password : `root`

Un fichier `data.sql` initialise les rôles, un admin, un manager, un utilisateur, des catégories et des livres de base.

## Endpoints principaux

- Authentification : `/api/auth`
- Livres : `/api/books`
- Emprunts : `/api/loans`
- Catégories : `/api/categories`
- Utilisateurs : `/api/users`
- Dashboard : `/api/dashboard`
- Export : `/api/export`

## Tests

- `mvn test`

## Notes

- Tous les contrôleurs renvoient un wrapper `ApiResponse<T>`.
- Les endpoints sont sécurisés avec des rôles et `@PreAuthorize`.
- La configuration CORS autorise `http://localhost:5173`.
