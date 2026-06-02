# 📚 Bibliothèque ENSAM — Système de Gestion de Bibliothèque

## 📖 Description

**Bibliothèque ENSAM** est une application web full-stack de gestion de bibliothèque universitaire, développée avec **Spring Boot 3** (Backend) et **React / TypeScript** (Frontend).

L'application offre une gestion complète :

- 🔐 Authentification sécurisée avec JWT (Access + Refresh Token)
- 👥 Gestion des utilisateurs et contrôle d'accès basé sur les rôles (RBAC)
- 📚 Gestion des livres avec catégories multiples
- 🔄 Gestion des emprunts et retours avec suivi de stock en temps réel
- 🔔 Notifications en temps réel via WebSocket (STOMP + SockJS)
- 🛡️ Verrouillage optimiste (`@Version`) pour la gestion concurrentielle du stock
- 📊 Tableau de bord statistique avec graphiques
- 📤 Export des données en PDF (iText 7) et Excel (Apache POI)
- 🌐 Interface multilingue (Français / Anglais) avec i18next
- 📄 Documentation API interactive avec Swagger / OpenAPI
- 🐳 Déploiement conteneurisé avec Docker Compose

---

## 🚀 Technologies Utilisées

### Backend (Spring Boot)

| Technologie                | Version  | Rôle                                      |
| -------------------------- | -------- | ----------------------------------------- |
| Java                       | 17       | Langage principal                         |
| Spring Boot                | 3.2.2    | Framework applicatif                      |
| Spring Web                 | —        | API REST                                  |
| Spring Data JPA            | —        | Couche de persistance (Hibernate/JPA)     |
| Spring Security            | —        | Authentification et autorisation          |
| Spring WebSocket           | —        | Notifications temps réel (STOMP/SockJS)   |
| Jakarta Validation         | —        | Validation des données d'entrée           |
| MapStruct                  | 1.5.5    | Mapping Entity ↔ DTO                      |
| Lombok                     | 1.18.32  | Réduction du code boilerplate             |
| JJWT                       | 0.11.5   | Génération et validation des tokens JWT   |
| SpringDoc OpenAPI          | 2.3.0    | Documentation Swagger automatique         |
| iText 7                    | 7.2.5    | Génération de fichiers PDF                |
| Apache POI                 | 5.2.5    | Génération de fichiers Excel (.xlsx)      |
| Maven                      | —        | Gestion des dépendances et build          |

### Frontend (React)

| Technologie         | Rôle                                |
| ------------------- | ----------------------------------- |
| React 18            | Bibliothèque UI                     |
| TypeScript          | Typage statique                     |
| Vite                | Bundler et serveur de développement |
| TanStack Router     | Routage côté client                 |
| TanStack React Query| Gestion du cache et des requêtes    |
| Material UI (MUI)   | Composants d'interface              |
| Recharts            | Graphiques et visualisations        |
| react-i18next       | Internationalisation (FR/EN)        |
| SockJS + STOMP      | Client WebSocket                    |
| Tailwind CSS        | Styles utilitaires                  |

### Base de données

| Technologie    | Utilisation        |
| -------------- | ------------------ |
| MySQL 8.0      | Production         |
| H2 Database    | Tests unitaires    |

### DevOps

| Technologie      | Rôle                           |
| ---------------- | ------------------------------ |
| Docker           | Conteneurisation               |
| Docker Compose   | Orchestration multi-conteneurs |
| phpMyAdmin       | Administration MySQL (UI)      |

---

## 🏗️ Architecture Backend

Le backend suit une **architecture en couches** (Layered Architecture) :

```
┌─────────────────────────────────────────────┐
│              Controller Layer               │
│  (REST API — Validation — Autorisation)     │
├─────────────────────────────────────────────┤
│               Service Layer                 │
│  (Logique métier — Transactions)            │
├─────────────────────────────────────────────┤
│             Repository Layer                │
│  (Spring Data JPA — Specifications)         │
├─────────────────────────────────────────────┤
│              Database (MySQL)               │
└─────────────────────────────────────────────┘
        ↕                         ↕
   DTO / Mapper              WebSocket
  (MapStruct)           (Notifications)
```

### Structure des packages

```
src/main/java/com/ensam/projet/
│
├── ProjetApplication.java          # Point d'entrée Spring Boot
│
├── config/
│   ├── CorsConfig.java             # Configuration CORS
│   ├── SecurityConfig.java         # Configuration Spring Security + JWT Filter
│   ├── SwaggerConfig.java          # Configuration OpenAPI / Swagger
│   └── WebSocketConfig.java        # Configuration STOMP WebSocket (endpoint + broker)
│
├── controller/
│   ├── AuthController.java         # Authentification (register, login, refresh, logout)
│   ├── BookController.java         # CRUD Livres + assignation catégories
│   ├── CategoryController.java     # CRUD Catégories
│   ├── DashboardController.java    # Statistiques du tableau de bord
│   ├── ExportController.java       # Export PDF et Excel (livres + emprunts)
│   ├── LoanController.java         # CRUD Emprunts + retour + mes emprunts
│   └── UserController.java         # Gestion utilisateurs + profil
│
├── dto/
│   ├── request/
│   │   ├── BookRequest.java
│   │   ├── CategoryRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── LoanRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── UpdateProfileRequest.java
│   └── response/
│       ├── ApiResponse.java            # Wrapper générique pour les réponses API
│       ├── AuthResponse.java
│       ├── BookResponse.java
│       ├── CategoryResponse.java
│       ├── CategoryStatDto.java
│       ├── DashboardStatsResponse.java
│       ├── LoanDetailResponse.java
│       ├── LoanResponse.java
│       ├── MonthlyStatDto.java
│       ├── PagedResponse.java          # Réponse paginée générique
│       └── UserResponse.java
│
├── entity/
│   ├── Book.java          # Entité Livre (avec @Version pour verrouillage optimiste)
│   ├── Category.java      # Entité Catégorie
│   ├── ERole.java         # Enum des rôles (ROLE_ADMIN, ROLE_MANAGER, ROLE_USER)
│   ├── Loan.java          # Entité Emprunt
│   ├── LoanDetail.java   # Détails de l'emprunt (notes, état, compteur renouvellement)
│   ├── LoanStatus.java    # Enum (ACTIVE, RETURNED, OVERDUE)
│   ├── Role.java          # Entité Rôle
│   └── User.java          # Entité Utilisateur
│
├── exception/
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java     # Gestionnaire global des exceptions (@RestControllerAdvice)
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
│
├── mapper/
│   ├── BookMapper.java         # Mapping Book ↔ BookResponse/BookRequest (MapStruct)
│   ├── CategoryMapper.java
│   ├── LoanDetailMapper.java
│   ├── LoanMapper.java
│   └── UserMapper.java
│
├── repository/
│   ├── BookRepository.java
│   ├── BookSpecification.java  # Specifications JPA pour recherche avancée
│   ├── CategoryRepository.java
│   ├── LoanDetailRepository.java
│   ├── LoanRepository.java
│   ├── RoleRepository.java
│   └── UserRepository.java
│
├── security/
│   ├── CustomUserDetailsService.java   # Chargement des utilisateurs depuis la BDD
│   ├── JwtAuthFilter.java             # Filtre JWT (OncePerRequestFilter)
│   └── JwtUtil.java                   # Utilitaire JWT (génération, validation, extraction)
│
└── service/
    ├── NotificationService.java        # Service de notifications WebSocket
    ├── interfaces/
    │   ├── AuthService.java
    │   ├── BookService.java
    │   ├── CategoryService.java
    │   ├── DashboardService.java
    │   ├── ExportService.java
    │   ├── LoanService.java
    │   └── UserService.java
    └── impl/
        ├── AuthServiceImpl.java        # Logique d'authentification (register, login, refresh)
        ├── BookServiceImpl.java        # Logique métier des livres
        ├── CategoryServiceImpl.java
        ├── DashboardServiceImpl.java   # Calcul des statistiques
        ├── ExportServiceImpl.java      # Génération PDF (iText) et Excel (POI)
        ├── LoanServiceImpl.java        # Logique métier des emprunts (stock, retour, etc.)
        └── UserServiceImpl.java        # Gestion des utilisateurs et profils
```

---

## 👥 Gestion des Rôles (RBAC)

Le système implémente un contrôle d'accès basé sur les rôles via `@PreAuthorize`.

| Fonctionnalité                   | ADMIN | MANAGER | USER |
| -------------------------------- | :---: | :-----: | :--: |
| Gestion des utilisateurs         |  ✅   |   ❌    |  ❌  |
| Gestion des rôles                |  ✅   |   ❌    |  ❌  |
| Activer/Désactiver un utilisateur|  ✅   |   ❌    |  ❌  |
| Supprimer un utilisateur         |  ✅   |   ❌    |  ❌  |
| Créer/Modifier un livre          |  ✅   |   ✅    |  ❌  |
| Supprimer un livre               |  ✅   |   ❌    |  ❌  |
| Créer/Modifier une catégorie     |  ✅   |   ✅    |  ❌  |
| Supprimer une catégorie          |  ✅   |   ❌    |  ❌  |
| Lister tous les emprunts         |  ✅   |   ✅    |  ❌  |
| Marquer un emprunt comme retourné|  ✅   |   ✅    |  ❌  |
| Créer un emprunt                 |  ✅   |   ✅    |  ✅  |
| Voir ses propres emprunts        |  ✅   |   ✅    |  ✅  |
| Consulter les livres             |  ✅   |   ✅    |  ✅  |
| Gérer son profil                 |  ✅   |   ✅    |  ✅  |
| Modifier son mot de passe        |  ✅   |   ✅    |  ✅  |
| Export PDF / Excel               |  ✅   |   ✅    |  ❌  |
| Voir le dashboard                |  ✅   |   ✅    |  ✅  |

---

## 🔐 Authentification JWT

Le système utilise JWT (JSON Web Token) avec un mécanisme **Access Token + Refresh Token**.

### Flux d'authentification

```
1. POST /api/auth/register  →  Inscription + retour du token
2. POST /api/auth/login     →  Connexion + retour accessToken + refreshToken
3. POST /api/auth/refresh   →  Renouvellement de l'accessToken expiré
4. POST /api/auth/logout    →  Déconnexion
```

### Sécurité

- Mots de passe hashés avec **BCrypt**
- Tokens signés avec **HMAC-SHA256**
- Filtre JWT (`JwtAuthFilter`) appliqué sur chaque requête
- Session **stateless** (pas de cookies de session)
- Endpoints publics : `/api/auth/**`, `/swagger-ui/**`, `/ws-endpoint/**`

---

## 🔔 Notifications en Temps Réel (WebSocket)

Le backend envoie des notifications en temps réel aux clients connectés via **STOMP over WebSocket**.

### Configuration

| Paramètre               | Valeur              |
| ------------------------ | ------------------- |
| Endpoint WebSocket       | `/ws-endpoint`      |
| Broker de messages       | `/topic`            |
| Préfixe d'application    | `/app`              |
| Fallback                 | SockJS              |

### Canal de notifications

```
/topic/notifications
```

### Types de notifications envoyées

| Événement                 | Titre              | Type      |
| ------------------------- | ------------------ | --------- |
| Livre emprunté            | Nouvel Emprunt     | `INFO`    |
| Livre retourné            | Livre Retourné     | `SUCCESS` |

### Payload JSON

```json
{
  "title": "Nouvel Emprunt",
  "message": "Le livre 'Clean Code' a été emprunté par ali",
  "type": "INFO"
}
```

Le frontend écoute ce canal et affiche une notification toast en temps réel pour les administrateurs.

---

## 🛡️ Verrouillage Optimiste (Concurrency Control)

L'entité `Book` utilise l'annotation `@Version` de JPA pour empêcher les **conditions de course** (race conditions) lors d'emprunts simultanés.

### Problème résolu

Si deux utilisateurs tentent d'emprunter le dernier exemplaire d'un livre **au même instant** :

```
Sans @Version :  User A emprunte ✅ + User B emprunte ✅  →  Stock = -1 ❌
Avec @Version :  User A emprunte ✅ + User B reçoit 409 Conflict ✅  →  Stock = 0 ✅
```

### Gestion de l'erreur

L'exception `ObjectOptimisticLockingFailureException` est interceptée par le `GlobalExceptionHandler` et retourne une réponse **HTTP 409 Conflict**.

---

## 📚 API Endpoints

### 🔑 Auth (`/api/auth`)

| Méthode | Endpoint           | Description                      | Accès    |
| ------- | ------------------ | -------------------------------- | -------- |
| POST    | `/api/auth/register` | Inscrire un nouvel utilisateur | Public   |
| POST    | `/api/auth/login`    | Se connecter                   | Public   |
| POST    | `/api/auth/refresh`  | Renouveler l'access token      | Public   |
| POST    | `/api/auth/logout`   | Se déconnecter                 | Public   |

### 📚 Books (`/api/books`)

| Méthode | Endpoint                      | Description                        | Accès          |
| ------- | ----------------------------- | ---------------------------------- | -------------- |
| GET     | `/api/books`                  | Lister les livres (paginé, trié, filtré) | Authentifié    |
| GET     | `/api/books/{id}`             | Récupérer un livre                 | Authentifié    |
| POST    | `/api/books`                  | Créer un livre                     | ADMIN, MANAGER |
| PUT     | `/api/books/{id}`             | Mettre à jour un livre             | ADMIN, MANAGER |
| DELETE  | `/api/books/{id}`             | Supprimer un livre                 | ADMIN          |
| POST    | `/api/books/{id}/categories`  | Assigner des catégories            | ADMIN, MANAGER |
| GET     | `/api/books/{id}/loans`       | Historique des emprunts d'un livre | ADMIN, MANAGER |

**Paramètres de recherche :** `page`, `size`, `sort`, `direction`, `search`, `categoryId`

### 🏷️ Categories (`/api/categories`)

| Méthode | Endpoint                 | Description              | Accès          |
| ------- | ------------------------ | ------------------------ | -------------- |
| GET     | `/api/categories`        | Lister toutes les catégories | Authentifié    |
| GET     | `/api/categories/{id}`   | Récupérer une catégorie  | Authentifié    |
| POST    | `/api/categories`        | Créer une catégorie      | ADMIN, MANAGER |
| PUT     | `/api/categories/{id}`   | Modifier une catégorie   | ADMIN, MANAGER |
| DELETE  | `/api/categories/{id}`   | Supprimer une catégorie  | ADMIN          |

### 🔄 Loans (`/api/loans`)

| Méthode | Endpoint                 | Description                        | Accès          |
| ------- | ------------------------ | ---------------------------------- | -------------- |
| GET     | `/api/loans`             | Lister tous les emprunts (filtrable par statut et userId) | ADMIN, MANAGER |
| GET     | `/api/loans/{id}`        | Récupérer un emprunt               | Authentifié*   |
| POST    | `/api/loans`             | Créer un emprunt (décrémenter le stock) | Tous les rôles |
| PUT     | `/api/loans/{id}`        | Modifier un emprunt                | ADMIN, MANAGER |
| PUT     | `/api/loans/{id}/return` | Retourner un livre (incrémenter le stock) | ADMIN, MANAGER |
| DELETE  | `/api/loans/{id}`        | Supprimer un emprunt               | ADMIN          |
| GET     | `/api/loans/my`          | Mes propres emprunts               | Tous les rôles |

> \* Un utilisateur ne peut consulter que ses propres emprunts. Les ADMIN/MANAGER peuvent voir tous les emprunts.

### 👤 Users (`/api/users`)

| Méthode | Endpoint                     | Description                        | Accès          |
| ------- | ---------------------------- | ---------------------------------- | -------------- |
| GET     | `/api/users`                 | Lister les utilisateurs            | ADMIN, MANAGER |
| GET     | `/api/users/{id}`            | Récupérer un utilisateur           | ADMIN          |
| PUT     | `/api/users/{id}/roles`      | Modifier les rôles                 | ADMIN          |
| PUT     | `/api/users/{id}/toggle`     | Activer/Désactiver un utilisateur  | ADMIN          |
| DELETE  | `/api/users/{id}`            | Supprimer un utilisateur           | ADMIN          |
| GET     | `/api/users/me`              | Mon profil                         | Authentifié    |
| PUT     | `/api/users/me`              | Mettre à jour mon profil           | Authentifié    |
| PUT     | `/api/users/me/password`     | Modifier mon mot de passe          | Authentifié    |

### 📊 Dashboard (`/api/dashboard`)

| Méthode | Endpoint               | Description                  | Accès       |
| ------- | ---------------------- | ---------------------------- | ----------- |
| GET     | `/api/dashboard/stats` | Statistiques du tableau de bord | Authentifié |

**Statistiques retournées :**
- `totalBooks` — Nombre total de livres
- `totalUsers` — Nombre total d'utilisateurs
- `activeLoans` — Emprunts actifs
- `overdueLoans` — Emprunts en retard
- `totalLoansThisMonth` — Emprunts du mois en cours
- `availableBooks` — Livres disponibles en stock
- `loansByMonth` — Courbe des emprunts par mois
- `booksByCategory` — Répartition des livres par catégorie

### 📤 Export (`/api/export`)

| Méthode | Endpoint                | Description               | Accès          |
| ------- | ----------------------- | ------------------------- | -------------- |
| GET     | `/api/export/pdf/books` | Exporter les livres en PDF | ADMIN, MANAGER |
| GET     | `/api/export/excel/books`| Exporter les livres en Excel | ADMIN, MANAGER |
| GET     | `/api/export/pdf/loans` | Exporter les emprunts en PDF | ADMIN, MANAGER |

---

## 🗄️ Modèle de Données

### Diagramme des entités

```
┌──────────────┐     N:N     ┌──────────────┐
│     User     │◄───────────►│     Role     │
│──────────────│             │──────────────│
│ id           │             │ id           │
│ username     │             │ name (ERole) │
│ email        │             └──────────────┘
│ password     │
│ enabled      │     1:N     ┌──────────────────┐
│ createdAt    │────────────►│      Loan        │
└──────────────┘             │──────────────────│
                             │ id               │
┌──────────────┐     1:N     │ loanDate         │
│     Book     │────────────►│ expectedReturn   │
│──────────────│             │ actualReturn     │
│ id           │             │ status (Enum)    │
│ title        │             └────────┬─────────┘
│ author       │                      │ 1:1
│ isbn         │             ┌────────┴─────────┐
│ description  │             │   LoanDetail     │
│ stock        │             │──────────────────│
│ publishedDate│             │ id               │
│ version (@V) │             │ notes            │
│ createdAt    │             │ itemCondition    │
│ updatedAt    │             │ renewalCount     │
└──────┬───────┘             │ returnedBy       │
       │ N:N                 └──────────────────┘
┌──────┴───────┐
│   Category   │
│──────────────│
│ id           │
│ name         │
│ description  │
│ color        │
└──────────────┘
```

### Relations

| Relation             | Type   | Table de jointure   |
| -------------------- | ------ | ------------------- |
| User ↔ Role          | N:N    | `user_roles`        |
| User → Loan          | 1:N    | FK `user_id`        |
| Book → Loan          | 1:N    | FK `book_id`        |
| Book ↔ Category      | N:N    | `book_categories`   |
| Loan → LoanDetail    | 1:1    | FK `loan_id`        |

### Enum : LoanStatus

| Valeur     | Description                        |
| ---------- | ---------------------------------- |
| `ACTIVE`   | Emprunt en cours                   |
| `RETURNED` | Livre retourné                     |
| `OVERDUE`  | Emprunt en retard                  |

### Enum : ERole

| Valeur         | Description     |
| -------------- | --------------- |
| `ROLE_ADMIN`   | Administrateur  |
| `ROLE_MANAGER` | Gestionnaire    |
| `ROLE_USER`    | Utilisateur     |

---

## ⚠️ Gestion des Exceptions

Le `GlobalExceptionHandler` (`@RestControllerAdvice`) intercepte toutes les exceptions et retourne une réponse `ApiResponse` standardisée :

| Exception                               | HTTP Status | Description                              |
| --------------------------------------- | ----------- | ---------------------------------------- |
| `ResourceNotFoundException`             | 404         | Ressource introuvable                    |
| `BadRequestException`                   | 400         | Requête invalide / Stock insuffisant     |
| `MethodArgumentNotValidException`       | 400         | Échec de validation (Jakarta Validation) |
| `AccessDeniedException`                 | 403         | Accès refusé (rôle insuffisant)          |
| `AuthenticationException`              | 401         | Non authentifié                          |
| `DataIntegrityViolationException`       | 409         | Conflit de données (contrainte unique)   |
| `ObjectOptimisticLockingFailureException` | 409       | Conflit de concurrence (stock épuisé)    |
| `Exception` (fallback)                  | 500         | Erreur serveur inattendue                |

### Format de réponse API

```json
{
  "success": true,
  "message": "Liste récupérée",
  "data": { ... },
  "timestamp": "2026-06-02T20:00:00"
}
```

```json
{
  "success": false,
  "message": "Stock insuffisant",
  "status": 400,
  "timestamp": "2026-06-02T20:00:00"
}
```

---

## 🐳 Déploiement avec Docker

### Services Docker Compose

| Service      | Image                  | Port Externe | Port Interne | Description            |
| ------------ | ---------------------- | ------------ | ------------ | ---------------------- |
| `mysql`      | `mysql:8.0`            | 3307         | 3306         | Base de données MySQL  |
| `phpmyadmin` | `phpmyadmin/phpmyadmin`| 8081         | 80           | Administration MySQL   |
| `backend`    | Build `./Backned`      | 8082         | 8080         | API Spring Boot        |
| `frontend`   | Build `./frontend`     | 5173         | 5173         | Application React      |

### Lancer le projet

```bash
# Cloner le projet
git clone https://github.com/yaarcoub/JEE-Project.git
cd JEE-Project

# Construire et démarrer tous les services
docker-compose build
docker-compose up -d

# Vérifier les logs
docker-compose logs -f backend
```

### Accès aux services

| Service        | URL                                  |
| -------------- | ------------------------------------ |
| Frontend       | http://localhost:5173                 |
| Backend API    | http://localhost:8082/api             |
| Swagger UI     | http://localhost:8082/swagger-ui.html |
| phpMyAdmin     | http://localhost:8081                 |

### Variables d'environnement (`.env`)

```env
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=ensam_bibliotheque

SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ensam_bibliotheque?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

JWT_SECRET=404E635266556A586E3272357538782F413F4428472B6250645367566B5970
```

---

## ⚙️ Installation Locale (sans Docker)

### Prérequis

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+

### Backend

```bash
cd Backned

# Configurer la base de données dans application.properties
# Puis compiler et lancer
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## 📄 Documentation API

| Interface      | URL                                          |
| -------------- | -------------------------------------------- |
| Swagger UI     | http://localhost:8080/swagger-ui.html         |
| Swagger UI     | http://localhost:8080/swagger-ui/index.html   |
| OpenAPI JSON   | http://localhost:8080/v3/api-docs             |

---

## 🧪 Tests

```bash
cd Backned
mvn test
```

Technologies de test :
- Spring Boot Test
- Spring Security Test
- Mockito JUnit Jupiter
- H2 Database (in-memory)

---

## 📌 Fonctionnalités Complètes

### Backend

- ✅ Authentification JWT (Access Token + Refresh Token)
- ✅ Contrôle d'accès basé sur les rôles (RBAC) avec `@PreAuthorize`
- ✅ Gestion des utilisateurs (CRUD, activation/désactivation, changement de mot de passe)
- ✅ Gestion des livres (CRUD, recherche avancée avec Specifications JPA, pagination, tri)
- ✅ Gestion des catégories (CRUD, assignation multiple aux livres)
- ✅ Gestion des emprunts (création, modification, retour, historique)
- ✅ Gestion automatique du stock (décrémentation à l'emprunt, incrémentation au retour)
- ✅ Verrouillage optimiste (`@Version`) pour empêcher les emprunts concurrents
- ✅ Notifications en temps réel via WebSocket (STOMP + SockJS)
- ✅ Dashboard statistique (totaux, emprunts par mois, livres par catégorie)
- ✅ Export PDF avec iText 7 (livres et emprunts)
- ✅ Export Excel avec Apache POI (livres)
- ✅ Validation des données avec Jakarta Validation (`@NotBlank`, `@Email`, `@Min`)
- ✅ Gestion centralisée des exceptions (`@RestControllerAdvice`)
- ✅ Réponses API standardisées (`ApiResponse<T>`)
- ✅ Mapping automatique Entity ↔ DTO avec MapStruct
- ✅ Documentation interactive Swagger / OpenAPI
- ✅ Profil de configuration (dev / prod)
- ✅ Gestion du profil utilisateur (modification username, email, mot de passe)

### Frontend

- ✅ Interface responsive et moderne (Material UI + Tailwind CSS)
- ✅ Mode sombre / clair
- ✅ Internationalisation Français / Anglais (i18next)
- ✅ Tableau de bord avec graphiques (Recharts : Pie, Bar, Radar)
- ✅ Carrousel de livres à la une avec rotation automatique
- ✅ Timeline d'activité (historique emprunts/retours)
- ✅ Notifications toast en temps réel (WebSocket)
- ✅ Gestion du cache avec TanStack React Query

---

## 👨‍💻 Auteur

Projet réalisé dans le cadre d'un projet académique **ENSAM** (École Nationale Supérieure d'Arts et Métiers).

Développé avec **Java 17**, **Spring Boot 3**, **React 18**, **TypeScript** et **MySQL 8**.
