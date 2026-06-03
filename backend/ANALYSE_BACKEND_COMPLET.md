# 📚 ANALYSE COMPLÈTE DU BACKEND - SYSTÈME DE GESTION DE BIBLIOTHÈQUE

## 🎯 Vue d'ensemble générale

Ce document fournit une analyse détaillée du backend Spring Boot 3.2+ pour la gestion d'une bibliothèque. Le projet est architecturé en couches suivant les bonnes pratiques enterprise avec:
- **JWT** pour l'authentification et l'autorisation
- **JPA/Hibernate** pour la persistence des données
- **Swagger** pour la documentation API
- **Export PDF/XLSX** pour les rapports
- **Architecture en couches** (Controller → Service → Repository)

---

## 📋 TABLE DES MATIÈRES

1. [Architecture générale](#architecture-générale)
2. [Modèle de données](#modèle-de-données)
3. [Couche de sécurité](#couche-de-sécurité)
4. [Endpoints API](#endpoints-api)
5. [Flux de données](#flux-de-données)
6. [Relations entre entités](#relations-entre-entités)
7. [Services métier](#services-métier)
8. [Exemples d'utilisation](#exemples-dutilisation)
9. [Configuration et déploiement](#configuration-et-déploiement)

---

## 🏗️ Architecture générale

### Structure en couches

```
┌─────────────────────────────────────────────────────┐
│          PRÉSENTATION (Controllers)                  │
│  AuthController, BookController, LoanController...  │
├─────────────────────────────────────────────────────┤
│            LOGIQUE MÉTIER (Services)                │
│  BookService, LoanService, UserService...          │
├─────────────────────────────────────────────────────┤
│          ACCÈS AUX DONNÉES (Repositories)          │
│  BookRepository, LoanRepository, UserRepository...  │
├─────────────────────────────────────────────────────┤
│              BASE DE DONNÉES (MySQL)               │
│  Tables: users, books, loans, categories...        │
└─────────────────────────────────────────────────────┘
```

### Stack technologique

| Composant | Version | Rôle |
|-----------|---------|------|
| Spring Boot | 3.2.2 | Framework web principal |
| Spring Security | 3.2.2 | Authentification et autorisation |
| Spring Data JPA | 3.2.2 | ORM et accès aux données |
| MySQL | 8.0+ | Base de données |
| JJWT | 0.11.5 | Tokens JWT |
| MapStruct | 1.5.5 | Mapping DTO ↔ Entity |
| SpringDoc OpenAPI | 2.3.0 | Documentation Swagger |
| iText | 7.2.5 | Génération PDF |
| Apache POI | 5.2.5 | Génération Excel |
| JUnit 5 | 5.9.2 | Tests unitaires |
| TestContainers | 1.17.6 | Tests d'intégration |

---

## 💾 Modèle de données

### Entités principales

#### 1️⃣ **User** (Utilisateur)

```
┌──────────────────────────────────────┐
│         USER                         │
├──────────────────────────────────────┤
│ id (PK)          : Long              │
│ username (UNIQUE): String            │
│ email (UNIQUE)   : String            │
│ password         : String (BCrypt)   │
│ enabled          : Boolean           │
│ createdAt        : LocalDateTime     │
├──────────────────────────────────────┤
│ Associations:                        │
│ - roles (ManyToMany)                │
│ - loans (OneToMany)                 │
└──────────────────────────────────────┘
```

**Rôle**: Représente un utilisateur du système
**Validations**: 
- Username unique et non vide
- Email unique et valide
- Password non vide

---

#### 2️⃣ **Role** (Rôle)

```
┌──────────────────────────────────────┐
│         ROLE                         │
├──────────────────────────────────────┤
│ id (PK)          : Long              │
│ name (UNIQUE)    : ERole (Enum)      │
├──────────────────────────────────────┤
│ ERole.ROLE_ADMIN                     │
│ ERole.ROLE_MANAGER                   │
│ ERole.ROLE_USER                      │
├──────────────────────────────────────┤
│ Associations:                        │
│ - users (ManyToMany)                │
└──────────────────────────────────────┘
```

**Rôles disponibles**:
- `ROLE_ADMIN`: Accès complet au système
- `ROLE_MANAGER`: Gestion des livres, emprunts et statistiques
- `ROLE_USER`: Accès limité (consultation, emprunts personnels)

---

#### 3️⃣ **Book** (Livre)

```
┌──────────────────────────────────────┐
│         BOOK                         │
├──────────────────────────────────────┤
│ id (PK)          : Long              │
│ title            : String (required) │
│ author           : String (required) │
│ isbn (UNIQUE)    : String            │
│ description      : String (TEXT)     │
│ stock            : Integer (≥0)      │
│ publishedDate    : LocalDate         │
│ createdAt        : LocalDateTime     │
│ updatedAt        : LocalDateTime     │
├──────────────────────────────────────┤
│ Associations:                        │
│ - loans (OneToMany)                 │
│ - categories (ManyToMany)           │
└──────────────────────────────────────┘
```

**Rôle**: Représente un livre de la bibliothèque
**Validations**: 
- Titre non vide
- Auteur non vide
- ISBN unique
- Stock ≥ 0

**Gestion du stock**:
- Le stock augmente quand un livre est retourné
- Le stock diminue quand un livre est emprunté

---

#### 4️⃣ **Category** (Catégorie)

```
┌──────────────────────────────────────┐
│       CATEGORY                       │
├──────────────────────────────────────┤
│ id (PK)          : Long              │
│ name (UNIQUE)    : String (required) │
│ description      : String            │
│ color            : String            │
├──────────────────────────────────────┤
│ Associations:                        │
│ - books (ManyToMany)                │
└──────────────────────────────────────┘
```

**Rôle**: Classification des livres
**Relation**: Plusieurs livres peuvent appartenir à plusieurs catégories

---

#### 5️⃣ **Loan** (Emprunt)

```
┌──────────────────────────────────────┐
│        LOAN                          │
├──────────────────────────────────────┤
│ id (PK)           : Long             │
│ loanDate          : LocalDate        │
│ expectedReturnDate: LocalDate        │
│ actualReturnDate  : LocalDate        │
│ status (Enum)     : LoanStatus       │
├──────────────────────────────────────┤
│ LoanStatus:                          │
│ - PENDING    (En attente)           │
│ - ACTIVE     (Actif/En cours)       │
│ - RETURNED   (Retourné)             │
│ - OVERDUE    (En retard)            │
├──────────────────────────────────────┤
│ Associations:                        │
│ - book (ManyToOne) [FK]             │
│ - user (ManyToOne) [FK]             │
│ - detail (OneToOne)                 │
└──────────────────────────────────────┘
```

**Rôle**: Enregistrement d'un emprunt de livre par un utilisateur

---

#### 6️⃣ **LoanDetail** (Détails d'emprunt)

```
┌──────────────────────────────────────┐
│      LOANDETAIL                      │
├──────────────────────────────────────┤
│ id (PK)           : Long             │
│ notes             : String (TEXT)    │
│ itemcondition     : String           │
│ renewalCount      : Integer          │
│ returnedBy        : String           │
├──────────────────────────────────────┤
│ Associations:                        │
│ - loan (OneToOne) [FK unique]       │
└──────────────────────────────────────┘
```

**Rôle**: Informations supplémentaires sur l'emprunt
- État du livre à l'emprunt/retour
- Nombre de renouvellements
- Notes du gestionnaire
- Qui a enregistré le retour

---

## 🔐 Couche de sécurité

### Architecture de sécurité

```
┌────────────────────────────────────────────────────────┐
│                 AUTHENTIFICATION JWT                   │
├────────────────────────────────────────────────────────┤
│  1. Login/Register → generate JWT tokens              │
│  2. Access Token (courte durée: 15 min)              │
│  3. Refresh Token (longue durée: 7 jours)            │
│  4. JwtAuthFilter valide chaque requête              │
│  5. @PreAuthorize sur les endpoints                  │
└────────────────────────────────────────────────────────┘
```

### Composants de sécurité

#### 1. **JwtUtil** - Gestion des tokens JWT

```java
Responsabilités:
- generateAccessToken()    → Token d'accès courte durée
- generateRefreshToken()   → Token de rafraîchissement
- extractUsername()        → Extrait le username du token
- isTokenValid()          → Valide le token
- isTokenExpired()        → Vérifie l'expiration
```

**Configuration des tokens**:
- **Secret**: Clé de signature Base64
- **Access Token**: 900,000 ms (15 minutes)
- **Refresh Token**: 604,800,000 ms (7 jours)
- **Algorithme**: HMAC-SHA256

#### 2. **JwtAuthFilter** - Filtre d'authentification

```
Flux pour chaque requête:
1. Extraire le token du header Authorization (Bearer scheme)
2. Valider le token avec JwtUtil
3. Charger l'utilisateur avec CustomUserDetailsService
4. Créer l'authentication context
5. Passer au SecurityFilterChain
```

#### 3. **CustomUserDetailsService** - Chargement des utilisateurs

```
Implémente UserDetailsService:
- loadUserByUsername()
- Récupère l'utilisateur from DB
- Retourne un UserDetails avec roles/permissions
```

#### 4. **SecurityConfig** - Configuration Spring Security

```
Configurations clés:
- CSRF désactivé (API stateless)
- CORS activé pour http://localhost:5173
- Session STATELESS (JWT au lieu de sessions)
- AuthenticationProvider avec BCrypt
- Endpoints publics: /api/auth/**, /swagger-ui/**, /v3/api-docs/**
- Tous autres endpoints: authentification requise
```

### Hiérarchie des rôles et permissions

| Endpoint | GET | POST | PUT | DELETE |
|----------|-----|------|-----|--------|
| `/api/books` | USER | ADMIN/MANAGER | ADMIN/MANAGER | ADMIN |
| `/api/loans` (all) | ADMIN/MANAGER | USER | ADMIN/MANAGER | ADMIN |
| `/api/loans/my` | USER | USER | - | - |
| `/api/categories` | USER | ADMIN/MANAGER | ADMIN/MANAGER | ADMIN |
| `/api/users` | ADMIN | - | ADMIN | ADMIN |
| `/api/dashboard` | ADMIN/MANAGER | - | - | - |
| `/api/export` | ADMIN/MANAGER | - | - | - |
| `/api/auth/login` | - | PUBLIC | - | - |
| `/api/auth/register` | - | PUBLIC | - | - |

---

## 📡 Endpoints API

### 1. 🔑 **AuthController** - `/api/auth`

Gère l'authentification et la gestion des sessions utilisateur.

#### **POST /api/auth/register**

```
Purpose: Créer un nouvel utilisateur
Method: POST
Auth Required: NO
Roles: PUBLIC

Request:
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}

Response: 201 Created
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "username": "john_doe",
    "email": "john@example.com"
  },
  "message": "Inscription réussie",
  "timestamp": "2024-01-15T10:30:00"
}

Validations:
- Username unique (sinon 400 "Nom d'utilisateur déjà utilisé")
- Email unique (sinon 400 "Email déjà utilisé")
- Password non vide
- Email format valide

Flow:
1. Vérifier unicité username/email
2. Encoder password avec BCrypt
3. Créer User entity
4. Assigner rôle ROLE_USER par défaut
5. Générer Access & Refresh tokens
6. Retourner AuthResponse
```

#### **POST /api/auth/login**

```
Purpose: Authentifier un utilisateur
Method: POST
Auth Required: NO
Roles: PUBLIC

Request:
{
  "username": "john_doe",
  "password": "SecurePass123"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "username": "john_doe",
    "email": "john@example.com"
  },
  "message": "Connexion réussie"
}

Error Cases:
- Username/Password invalide → 401 Unauthorized
- User non activé → 401 Unauthorized

Flow:
1. AuthenticationManager authentifie credentials
2. Récupérer User from DB
3. Générer JWT tokens
4. Retourner AuthResponse
```

#### **POST /api/auth/refresh**

```
Purpose: Renouveler le token d'accès
Method: POST
Auth Required: YES
Query Params: refreshToken (String)

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "nouveau_token...",
    "refreshToken": "nouveau_refresh_token...",
    "tokenType": "Bearer",
    ...
  },
  "message": "Token rafraîchi"
}

Error Cases:
- Refresh token invalide → 401
- Refresh token expiré → 401

Flow:
1. Valider le refreshToken
2. Extraire username du token
3. Récupérer User
4. Générer nouveaux Access & Refresh tokens
5. Retourner les nouveaux tokens
```

#### **POST /api/auth/logout**

```
Purpose: Déconnexion de l'utilisateur
Method: POST
Auth Required: YES

Response: 200 OK
{
  "success": true,
  "data": null,
  "message": "Déconnexion réussie"
}

Note: JWT est stateless. La déconnexion côté client 
consiste simplement à supprimer le token local.
Le backend maintient une blacklist optionnelle.
```

---

### 2. 📚 **BookController** - `/api/books`

Gestion complète des livres de la bibliothèque.

#### **GET /api/books**

```
Purpose: Lister les livres avec pagination/filtrage
Method: GET
Auth Required: YES (isAuthenticated)
Roles: All authenticated users

Query Parameters:
- page: Integer (default: 0)          → Numéro de page
- size: Integer (default: 10)         → Éléments par page
- sort: String (default: "title")     → Champ de tri
- direction: String (default: "asc")  → asc ou desc
- search: String (optional)           → Recherche par titre/auteur
- categoryId: Long (optional)         → Filtrer par catégorie

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Le Seigneur des Anneaux",
        "author": "J.R.R. Tolkien",
        "isbn": "978-2253045656",
        "description": "...",
        "stock": 5,
        "publishedDate": "1954-07-29",
        "categories": [
          {"id": 1, "name": "Fantasy"}
        ],
        "createdAt": "2024-01-10T10:00:00"
      },
      ...
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 10
  },
  "message": "Liste récupérée"
}

Filtrage & Recherche:
- search s'applique sur title ET author (OR)
- categoryId filtre les livres de cette catégorie
- sort + direction permettent tout tri
- Pagination: page 0 = première page

Flow:
1. Récupérer paramètres de pagination
2. Construire Specification pour filtrage
3. Interroger BookRepository avec pagination
4. Mapper entities vers BookResponse
5. Retourner PagedResponse
```

#### **GET /api/books/{id}**

```
Purpose: Récupérer les détails d'un livre
Method: GET
Auth Required: YES
Path Variable: id (Long)

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Le Seigneur des Anneaux",
    ...
  },
  "message": "Livre récupéré"
}

Error Cases:
- Livre non trouvé → 404 Not Found
```

#### **POST /api/books**

```
Purpose: Créer un nouveau livre
Method: POST
Auth Required: YES
Roles: ADMIN, MANAGER

Request:
{
  "title": "Nouveau Livre",
  "author": "Auteur",
  "isbn": "978-2253045656",
  "description": "Description du livre",
  "stock": 10,
  "publishedDate": "2024-01-01"
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 25,
    "title": "Nouveau Livre",
    ...
  },
  "message": "Livre créé"
}

Validations:
- Title non vide
- Author non vide
- ISBN unique
- Stock ≥ 0

Flow:
1. Valider les données
2. Vérifier ISBN unique
3. Créer Book entity
4. Persister en DB
5. Retourner BookResponse
```

#### **PUT /api/books/{id}**

```
Purpose: Mettre à jour un livre
Method: PUT
Auth Required: YES
Roles: ADMIN, MANAGER
Path Variable: id (Long)

Request:
{
  "title": "Titre mis à jour",
  "author": "Nouvel auteur",
  "stock": 15,
  ...
}

Response: 200 OK
{
  "success": true,
  "data": {...},
  "message": "Livre mis à jour"
}

Error Cases:
- Livre non trouvé → 404
- ISBN déjà utilisé → 400
```

#### **DELETE /api/books/{id}**

```
Purpose: Supprimer un livre
Method: DELETE
Auth Required: YES
Roles: ADMIN
Path Variable: id (Long)

Response: 204 No Content

Logic:
- Supprimer tous les emprunts associés (cascade)
- Supprimer les associations avec catégories
- Supprimer le livre
```

#### **POST /api/books/{id}/categories**

```
Purpose: Assigner des catégories à un livre
Method: POST
Auth Required: YES
Roles: ADMIN, MANAGER
Path Variable: id (Long)

Request Body:
[1, 2, 5]  → IDs des catégories

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "categories": [
      {"id": 1, "name": "Fantasy"},
      {"id": 2, "name": "Adventure"},
      {"id": 5, "name": "Classics"}
    ]
  },
  "message": "Catégories assignées"
}

Flow:
1. Récupérer le livre
2. Charger les catégories par IDs
3. Remplacer l'ensemble des catégories
4. Persister
5. Retourner le livre mis à jour
```

#### **GET /api/books/{id}/loans**

```
Purpose: Lister les emprunts d'un livre
Method: GET
Auth Required: YES
Roles: ADMIN, MANAGER
Path Variable: id (Long)

Query Parameters:
- page: Integer (default: 0)
- size: Integer (default: 10)

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "book": {...},
        "user": {...},
        "loanDate": "2024-01-10",
        "expectedReturnDate": "2024-01-24",
        "status": "ACTIVE"
      },
      ...
    ],
    "totalElements": 5,
    "totalPages": 1
  }
}

Utilité: Historique complet des emprunts d'un livre
```

---

### 3. 🔄 **LoanController** - `/api/loans`

Gestion des emprunts et retours de livres.

#### **GET /api/loans** (Admin/Manager uniquement)

```
Purpose: Lister tous les emprunts
Auth Required: YES
Roles: ADMIN, MANAGER

Query Parameters:
- page: Integer (default: 0)
- size: Integer (default: 10)
- status: LoanStatus (optional)     → PENDING, ACTIVE, RETURNED, OVERDUE
- userId: Long (optional)            → Filtrer par utilisateur

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "user": {"id": 1, "username": "john_doe", ...},
        "book": {"id": 5, "title": "...", ...},
        "loanDate": "2024-01-10",
        "expectedReturnDate": "2024-01-24",
        "actualReturnDate": null,
        "status": "ACTIVE",
        "detail": {...}
      },
      ...
    ],
    "totalElements": 42,
    "totalPages": 5
  }
}

Filtering Options:
- status: Filtrer par état du prêt
- userId: Voir tous les emprunts d'un utilisateur
```

#### **GET /api/loans/{id}**

```
Purpose: Récupérer les détails d'un emprunt
Auth Required: YES

Access Control:
- ADMIN/MANAGER: Accès complet
- USER: Accès uniquement à ses propres emprunts

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "user": {...},
    "book": {...},
    "loanDate": "2024-01-10",
    "expectedReturnDate": "2024-01-24",
    "actualReturnDate": null,
    "status": "ACTIVE",
    "detail": {
      "id": 1,
      "notes": "En bon état",
      "itemcondition": "Excellent",
      "renewalCount": 0,
      "returnedBy": null
    }
  }
}

Error Cases:
- ID non trouvé → 404
- User essayant d'accéder à l'emprunt d'un autre → 403 Forbidden
```

#### **POST /api/loans**

```
Purpose: Créer un nouvel emprunt
Auth Required: YES
Roles: ADMIN, MANAGER, USER

Request:
{
  "bookId": 5,
  "userId": 1,
  "loanDate": "2024-01-15",
  "expectedReturnDate": "2024-01-29"
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 25,
    "book": {...},
    "user": {...},
    "status": "ACTIVE",
    ...
  },
  "message": "Emprunt créé"
}

Business Logic:
1. Vérifier que le livre existe et stock > 0
2. Vérifier que l'utilisateur existe et est actif
3. Décrémenter le stock du livre
4. Créer Loan avec status ACTIVE
5. Créer LoanDetail associé
6. Persister les deux

Validations:
- Stock disponible
- Utilisateur actif
- Dates cohérentes (expectedReturnDate > loanDate)

Error Cases:
- Stock épuisé → 400 "Stock insuffisant"
- Utilisateur non trouvé → 404
- Utilisateur désactivé → 400
```

#### **PUT /api/loans/{id}**

```
Purpose: Mettre à jour un emprunt
Auth Required: YES
Roles: ADMIN, MANAGER
Path Variable: id (Long)

Request:
{
  "expectedReturnDate": "2024-02-05",
  "notes": "Mise à jour des notes"
}

Response: 200 OK
```

#### **PUT /api/loans/{id}/return**

```
Purpose: Marquer un emprunt comme retourné
Auth Required: YES
Roles: ADMIN, MANAGER
Path Variable: id (Long)
Query Param: returnedBy (String)

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "status": "RETURNED",
    "actualReturnDate": "2024-01-20",
    "detail": {
      "returnedBy": "Agent_123",
      "itemcondition": "Bon état"
    }
  }
}

Business Logic:
1. Récupérer le Loan
2. Vérifier status = ACTIVE ou OVERDUE
3. Incrémenter le stock du livre
4. Mettre à jour actualReturnDate
5. Changer status à RETURNED
6. Mettre à jour LoanDetail avec returnedBy
7. Persister

Validation:
- Seuls ACTIVE/OVERDUE peuvent être retournés
```

#### **DELETE /api/loans/{id}**

```
Purpose: Supprimer un emprunt
Auth Required: YES
Roles: ADMIN
Path Variable: id (Long)

Response: 204 No Content

Logic:
- Supprimer le LoanDetail associé
- Supprimer le Loan
- Restaurer le stock si applicable
```

#### **GET /api/loans/my**

```
Purpose: Récupérer mes propres emprunts
Auth Required: YES
Roles: ADMIN, MANAGER, USER

Query Parameters:
- page: Integer (default: 0)
- size: Integer (default: 10)

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {...loans de l'utilisateur courant...}
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}

Logic:
1. Récupérer l'utilisateur from SecurityContext
2. Récupérer ses emprunts paginés
3. Retourner PagedResponse
```

---

### 4. 📂 **CategoryController** - `/api/categories`

Gestion des catégories de livres.

#### **GET /api/categories**

```
Purpose: Lister toutes les catégories
Auth Required: YES
Response: 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Fantasy",
      "description": "Livres de fantasy",
      "color": "#FF5733"
    },
    ...
  ]
}

Note: Non paginé (liste simple)
```

#### **GET /api/categories/{id}**

```
Purpose: Récupérer une catégorie
Auth Required: YES

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Fantasy",
    "description": "...",
    "color": "#FF5733",
    "bookCount": 12
  }
}
```

#### **POST /api/categories**

```
Purpose: Créer une catégorie
Auth Required: YES
Roles: ADMIN, MANAGER

Request:
{
  "name": "Science-Fiction",
  "description": "Livres de science-fiction",
  "color": "#00FF00"
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": 10,
    "name": "Science-Fiction",
    ...
  }
}

Validations:
- Name unique et non vide
- Color format hexadécimal optionnel
```

#### **PUT /api/categories/{id}**

```
Purpose: Mettre à jour une catégorie
Auth Required: YES
Roles: ADMIN, MANAGER
```

#### **DELETE /api/categories/{id}**

```
Purpose: Supprimer une catégorie
Auth Required: YES
Roles: ADMIN

Logic:
- Supprimer les associations book_categories
- Supprimer la catégorie
```

---

### 5. 👥 **UserController** - `/api/users`

Gestion des utilisateurs (Admin uniquement, sauf /me).

#### **GET /api/users**

```
Purpose: Lister tous les utilisateurs
Auth Required: YES
Roles: ADMIN

Query Parameters:
- page: Integer (default: 0)
- size: Integer (default: 10)

Response: 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "roles": ["ROLE_USER"],
        "enabled": true,
        "createdAt": "2024-01-10"
      },
      ...
    ],
    "totalElements": 15,
    "totalPages": 2
  }
}
```

#### **GET /api/users/{id}**

```
Purpose: Récupérer les détails d'un utilisateur
Auth Required: YES
Roles: ADMIN

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"],
    "enabled": true,
    "createdAt": "2024-01-10T10:00:00",
    "loanCount": 3
  }
}
```

#### **PUT /api/users/{id}/roles**

```
Purpose: Mettre à jour les rôles d'un utilisateur
Auth Required: YES
Roles: ADMIN
Path Variable: id (Long)

Request Body:
["ROLE_ADMIN", "ROLE_MANAGER"]

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "roles": ["ROLE_ADMIN", "ROLE_MANAGER"],
    ...
  },
  "message": "Rôles mis à jour"
}

Business Logic:
1. Récupérer l'utilisateur
2. Récupérer les roles from DB
3. Remplacer l'ensemble des rôles
4. Persister
5. Retourner l'utilisateur mis à jour
```

#### **PUT /api/users/{id}/toggle**

```
Purpose: Activer/Désactiver un utilisateur
Auth Required: YES
Roles: ADMIN
Path Variable: id (Long)

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "enabled": false,
    ...
  },
  "message": "Statut de l'utilisateur mis à jour"
}

Effect:
- User désactivé ne peut plus se connecter
- Sessions existantes restent valides (stateless JWT)
```

#### **DELETE /api/users/{id}**

```
Purpose: Supprimer un utilisateur
Auth Required: YES
Roles: ADMIN
Path Variable: id (Long)

Response: 204 No Content

Logic:
- Supprimer tous les emprunts de l'utilisateur (cascade)
- Supprimer l'utilisateur
- Restaurer les stocks des livres empruntés
```

#### **GET /api/users/me**

```
Purpose: Récupérer mon profil
Auth Required: YES
Accessible By: All authenticated users

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"],
    "enabled": true,
    "createdAt": "2024-01-10"
  }
}

Flow:
1. Récupérer username from SecurityContext
2. Récupérer User from DB
3. Mapper vers UserResponse
4. Retourner
```

#### **PUT /api/users/me**

```
Purpose: Mettre à jour mon profil
Auth Required: YES
Accessible By: All authenticated users

Request:
{
  "username": "new_username",
  "email": "newemail@example.com"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "username": "new_username",
    "email": "newemail@example.com",
    ...
  }
}

Validations:
- Nouvel username unique (sauf le courant)
- Nouvel email unique (sauf le courant)
- Email format valide
```

---

### 6. 📊 **DashboardController** - `/api/dashboard`

Statistiques et analytics de l'application.

#### **GET /api/dashboard/stats**

```
Purpose: Récupérer les statistiques du dashboard
Auth Required: YES
Roles: ADMIN, MANAGER

Response: 200 OK
{
  "success": true,
  "data": {
    "totalBooks": 150,
    "totalUsers": 45,
    "totalLoans": 120,
    "activeLoans": 28,
    "overdueLoans": 5,
    "returnedLoans": 87,
    "totalCategories": 12,
    "averageLoanDuration": 14,
    "bookCategories": [
      {
        "category": "Fantasy",
        "count": 35
      },
      ...
    ],
    "monthlyStats": [
      {
        "month": "January",
        "loans": 12,
        "returns": 10
      },
      ...
    ],
    "topBooks": [
      {
        "id": 5,
        "title": "Most Borrowed Book",
        "borrowCount": 25
      },
      ...
    ]
  },
  "message": "Statistiques récupérées"
}

Métriques calculées:
- totalBooks: Nombre total de livres
- totalUsers: Nombre total d'utilisateurs
- totalLoans: Tous les emprunts
- activeLoans: Status = ACTIVE
- overdueLoans: Status = OVERDUE
- averageLoanDuration: Jours moyen entre loanDate et returnDate
- bookCategories: Nombre de livres par catégorie
- monthlyStats: Emprunts/Retours par mois (12 derniers mois)
- topBooks: 10 livres les plus empruntés
```

---

### 7. 📤 **ExportController** - `/api/export`

Export des données en formats PDF et Excel.

#### **GET /api/export/pdf/books**

```
Purpose: Exporter la liste des livres en PDF
Auth Required: YES
Roles: ADMIN, MANAGER

Response: 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename=books.pdf

PDF Contenu:
- Header: "Liste des Livres"
- Tableau avec colonnes:
  - Titre
  - Auteur
  - ISBN
  - Stock
  - Catégories
  - Date de création

Implémentation: iText (com.itextpdf)
```

#### **GET /api/export/excel/books**

```
Purpose: Exporter la liste des livres en Excel
Auth Required: YES
Roles: ADMIN, MANAGER

Response: 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename=books.xlsx

Excel Contenu:
- Feuille "Books"
- Colonnes: ID, Titre, Auteur, ISBN, Stock, Catégories, CreatedAt
- Rows: Tous les livres (limité à 1000 par défaut)

Implémentation: Apache POI
```

---

## 🔀 Flux de données

### 1. Flux d'authentification

```
┌─────────────────────────────────────────────────────────────┐
│                    AUTHENTIFICATION FLOW                    │
├─────────────────────────────────────────────────────────────┤

USER INPUT (Client/Frontend)
    ↓
    │  Username + Password
    ↓
POST /api/auth/login
    ↓
AuthController.login()
    ↓
AuthenticationManager.authenticate()
    ↓
DaoAuthenticationProvider
    ↓
CustomUserDetailsService.loadUserByUsername()
    ↓
UserRepository.findByUsername()
    ↓
BCryptPasswordEncoder.matches()
    ↓
    ├─ Password INCORRECT → AuthenticationException (401)
    │
    └─ Password CORRECT → Continue
    ↓
AuthServiceImpl.generateAuthResponse()
    ↓
JwtUtil.generateAccessToken()    → Token valide 15 min
JwtUtil.generateRefreshToken()   → Token valide 7 jours
    ↓
AuthResponse RETOURNÉ AU CLIENT
    ↓
CLIENT STOCKE LE TOKEN (localStorage)
    ├─ Access Token: Bearer {token}
    └─ Refresh Token: Pour renouvellement

SUBSEQUENT REQUESTS:
    ↓
Header: Authorization: Bearer {accessToken}
    ↓
JwtAuthFilter (à chaque requête)
    ↓
JwtUtil.isTokenValid()
    ├─ Token EXPIRÉ / INVALIDE → 401 Unauthorized
    │
    └─ Token VALIDE → Continue
    ↓
CustomUserDetailsService.loadUserByUsername()
    ↓
SecurityContext MIS À JOUR
    ↓
@PreAuthorize("hasRole(...)")
    ├─ Rôle INSUFFISANT → 403 Forbidden
    │
    └─ Rôle OK → Continue
    ↓
HANDLER EXÉCUTÉ
```

### 2. Flux de création d'emprunt

```
┌─────────────────────────────────────────────────────────────┐
│              FLUX CRÉATION D'EMPRUNT (Loan)                 │
├─────────────────────────────────────────────────────────────┤

POST /api/loans
Request: { bookId, userId, loanDate, expectedReturnDate }
    ↓
LoanController.createLoan()
    ↓
    ├─ Validation: expectedReturnDate > loanDate
    │
    └─ Continue
    ↓
LoanServiceImpl.createLoan()
    ↓
BookRepository.findById(bookId)
    ├─ Livre NOT FOUND → Exception (404)
    ├─ Stock = 0 → Exception (400 "Stock insuffisant")
    │
    └─ Continue
    ↓
UserRepository.findById(userId)
    ├─ User NOT FOUND → Exception (404)
    ├─ User.enabled = false → Exception (400)
    │
    └─ Continue
    ↓
CRÉER LOAN ENTITY
    ├─ status = ACTIVE
    ├─ loanDate = {input}
    ├─ expectedReturnDate = {input}
    └─ actualReturnDate = null
    ↓
CRÉER LOANDETAIL ENTITY
    ├─ renewalCount = 0
    ├─ notes = ""
    └─ returnedBy = null
    ↓
Book.stock-- (Décrémenter le stock)
    ↓
LoanRepository.save(loan)
LoanDetailRepository.save(loanDetail)
BookRepository.save(book)
    ↓
RETOURNER LoanResponse (201 Created)
```

### 3. Flux de retour d'emprunt

```
┌─────────────────────────────────────────────────────────────┐
│              FLUX RETOUR D'EMPRUNT (Return)                 │
├─────────────────────────────────────────────────────────────┤

PUT /api/loans/{id}/return?returnedBy=Agent_123
    ↓
LoanController.returnLoan()
    ↓
LoanServiceImpl.returnLoan()
    ↓
LoanRepository.findById(id)
    ├─ NOT FOUND → Exception (404)
    │
    └─ Continue
    ↓
Loan.status = ACTIVE or OVERDUE?
    ├─ NO (status = PENDING/RETURNED) → Exception
    │
    └─ YES → Continue
    ↓
Book.stock++ (Incrémenter le stock)
    ↓
Loan.status = RETURNED
Loan.actualReturnDate = Today
LoanDetail.returnedBy = {returnedBy}
LoanDetail.itemcondition = {request}
    ↓
LoanRepository.save(loan)
BookRepository.save(book)
    ↓
RETOURNER LoanResponse (200 OK)
```

---

## 🔗 Relations entre entités

### Diagramme ER (Entity Relationship)

```
                    ┌─────────────────────┐
                    │       ROLE          │
                    ├─────────────────────┤
                    │ id (PK)             │
                    │ name (Enum)         │
                    │ ┌─────────────────┐ │
                    │ │ ROLE_ADMIN      │ │
                    │ │ ROLE_MANAGER    │ │
                    │ │ ROLE_USER       │ │
                    │ └─────────────────┘ │
                    └──────────┬──────────┘
                               │
                         (ManyToMany)
                               │
        ┌──────────────────────┴──────────────────────┐
        │                                             │
        │                                             │
┌───────▼─────────┐                          ┌───────▼─────────┐
│      USER       │◄─────────(OneToMany)─────│      LOAN       │
├─────────────────┤                          ├─────────────────┤
│ id (PK)         │                          │ id (PK)         │
│ username (UQ)   │                          │ loanDate        │
│ email (UQ)      │                          │ expectedReturn  │
│ password        │                          │ actualReturn    │
│ enabled         │                          │ status (Enum)   │
│ createdAt       │                          │ ┌─────────────┐ │
│ roles (FK)      │                          │ │ PENDING     │ │
└─────────────────┘                          │ │ ACTIVE      │ │
                                             │ │ RETURNED    │ │
                                             │ │ OVERDUE     │ │
                                             │ └─────────────┘ │
                                             │ user_id (FK)    │
                                             │ book_id (FK)    │
                                             └────────┬────────┘
                                                      │
                                              (OneToOne)
                                                      │
                                             ┌────────▼────────┐
                                             │  LOANDETAIL     │
                                             ├─────────────────┤
                                             │ id (PK)         │
                                             │ notes           │
                                             │ itemcondition   │
                                             │ renewalCount    │
                                             │ returnedBy      │
                                             │ loan_id (FK UQ) │
                                             └─────────────────┘

                             ┌──────────────────────────┐
                             │        BOOK              │
                             ├──────────────────────────┤
                             │ id (PK)                  │
                             │ title                    │
                             │ author                   │
                             │ isbn (UQ)                │
                             │ description              │
                             │ stock                    │
                             │ publishedDate            │
                             │ createdAt                │
                             │ updatedAt                │
                             └──────────┬───────────────┘
                                        │
                          (OneToMany/ManyToOne)
                                        │
                                  (voir LOAN)
                                        │
                            (ManyToMany via JoinTable)
                                        │
                      ┌─────────────────┴─────────────────┐
                      │                                   │
                      │                                   │
              ┌───────▼─────────┐              ┌──────────▼──────┐
              │    CATEGORY     │              │  book_categories│
              ├─────────────────┤              │ (Join Table)    │
              │ id (PK)         │              ├─────────────────┤
              │ name (UQ)       │              │ book_id (FK)    │
              │ description     │              │ category_id (FK)│
              │ color           │              └─────────────────┘
              │ books (FK)      │
              └─────────────────┘
```

### Explications des relations

#### 1. **User ↔ Role** (ManyToMany)
- Un utilisateur peut avoir plusieurs rôles
- Un rôle peut être assigné à plusieurs utilisateurs
- Table de jonction: `user_roles`
- **Exemple**: John peut être ADMIN et MANAGER

#### 2. **User ↔ Loan** (OneToMany)
- Un utilisateur peut avoir plusieurs emprunts
- Un emprunt appartient à un seul utilisateur
- **Cascade**: Si un user est supprimé, ses loans sont supprimées

#### 3. **Book ↔ Loan** (OneToMany)
- Un livre peut avoir plusieurs emprunts
- Un emprunt concerne un seul livre
- **Cascade**: Si un book est supprimé, ses loans sont supprimées

#### 4. **Loan ↔ LoanDetail** (OneToOne)
- Un emprunt a exactement un détail
- Un détail appartient à un seul emprunt
- **Unique**: Une LoanDetail ne peut être liée qu'à un seul Loan
- **Orphan Removal**: Si le Loan est supprimé, le LoanDetail aussi

#### 5. **Book ↔ Category** (ManyToMany)
- Un livre peut appartenir à plusieurs catégories
- Une catégorie peut contenir plusieurs livres
- Table de jonction: `book_categories`
- **Exemple**: "Le Seigneur des Anneaux" → Fantasy, Adventure, Classics

---

## 🛠️ Services métier

### Architecture des services

```
┌──────────────────────────┐
│   Service Interface      │
│  (contrat public)        │
├──────────────────────────┤
│ BookService              │
│ LoanService              │
│ UserService              │
│ CategoryService          │
│ AuthService              │
│ DashboardService         │
│ ExportService            │
└──────────────────────────┘
         ▲
         │ (implements)
         │
┌────────┴─────────────────────────┐
│   Service Implementation         │
│   (logique métier concrète)     │
├─────────────────────────────────┤
│ BookServiceImpl                  │
│ LoanServiceImpl                  │
│ UserServiceImpl                  │
│ CategoryServiceImpl              │
│ AuthServiceImpl                  │
│ DashboardServiceImpl             │
│ ExportServiceImpl                │
└─────────────────────────────────┘
```

### 1. **BookService** - Gestion des livres

```
Responsabilités principales:
├─ getAllBooks()          → Récupère avec pagination/filtrage
├─ getBookById()          → Récupérer un livre
├─ createBook()           → Créer un nouveau livre
├─ updateBook()           → Mettre à jour un livre
├─ deleteBook()           → Supprimer un livre
├─ assignCategories()     → Assigner des catégories
└─ getBookLoans()         → Historique des emprunts

Utilise:
├─ BookRepository         → Accès aux données
├─ CategoryRepository     → Chargement des catégories
├─ BookMapper (MapStruct) → Entity ↔ DTO mapping
└─ BookSpecification      → Filtrage dynamique

Validation métier:
├─ ISBN doit être unique
├─ Titre et Auteur requis
├─ Stock ≥ 0
└─ Livre ne peut être supprimé s'il a des emprunts actifs
```

### 2. **LoanService** - Gestion des emprunts

```
Responsabilités principales:
├─ getAllLoans()          → Lister avec filtrage
├─ getLoanById()          → Récupérer un emprunt
├─ createLoan()           → Créer un emprunt
├─ updateLoan()           → Mettre à jour
├─ returnLoan()           → Marquer comme retourné
├─ deleteLoan()           → Supprimer
└─ getMyLoans()           → Mes emprunts

Logique métier complexe:
├─ Décrémentation du stock à la création
├─ Incrémentation du stock au retour
├─ Vérification du statut ACTIVE/OVERDUE avant retour
├─ Calcul automatique du statut OVERDUE
│   (si actualReturnDate > expectedReturnDate)
└─ Gestion des détails d'emprunt (notes, condition)

Validations métier:
├─ Livre doit exister et avoir du stock
├─ Utilisateur doit exister et être activé
├─ Dates cohérentes
└─ Statut cohérent (PENDING → ACTIVE → RETURNED)
```

### 3. **AuthService** - Authentification

```
Responsabilités:
├─ register()             → Inscription nouvel utilisateur
├─ login()                → Authentification
├─ refreshToken()         → Renouvellement du token
└─ logout()               → Déconnexion

Logique métier:
├─ Vérifier unicité username/email
├─ Encoder le password avec BCrypt
├─ Assigner rôle ROLE_USER par défaut
├─ Générer tokens JWT (Access + Refresh)
└─ Valider credentials lors du login

Sécurité:
├─ Pas de stockage en clair des passwords
├─ Tokens avec expiration
└─ Refresh token pour renouvellement sans re-login
```

### 4. **UserService** - Gestion utilisateurs

```
Responsabilités:
├─ getAllUsers()          → Lister (paginated)
├─ getUserById()          → Récupérer un user
├─ updateRoles()          → Modifier les rôles
├─ toggleEnabled()        → Activer/Désactiver
├─ deleteUser()           → Supprimer
├─ getCurrentUser()        → L'utilisateur connecté
└─ updateProfile()        → Mise à jour profil

Validations:
├─ Nouvel username/email doit être unique
├─ Un user ne peut pas supprimer ses propres rôles ADMIN
└─ Un user ne peut pas se désactiver soi-même
```

### 5. **CategoryService** - Gestion des catégories

```
Responsabilités:
├─ getAllCategories()     → Lister (simple list)
├─ getCategoryById()      → Récupérer une catégorie
├─ createCategory()       → Créer
├─ updateCategory()       → Mettre à jour
└─ deleteCategory()       → Supprimer

Logique:
├─ Nom unique pour chaque catégorie
├─ Suppression: remplacer associations ou cascade?
└─ Couleur optionnelle (pour le frontend)
```

### 6. **DashboardService** - Statistiques

```
Responsabilités:
└─ getDashboardStats()    → Calcule toutes les stats

Calculs:
├─ totalBooks            → COUNT(*) FROM books
├─ totalUsers            → COUNT(*) FROM users
├─ totalLoans            → COUNT(*) FROM loans
├─ activeLoans           → COUNT(*) WHERE status = 'ACTIVE'
├─ overdueLoans          → COUNT(*) WHERE status = 'OVERDUE'
├─ booksByCategory       → GROUP BY category
├─ monthlyStats          → GROUP BY MONTH(loanDate)
├─ topBooks              → TOP 10 by borrow count
└─ averageLoanDuration   → AVG(DATEDIFF(returnDate, loanDate))

Performance: Peut utiliser des caches ou des requêtes optimisées
```

### 7. **ExportService** - Export de données

```
Responsabilités:
├─ exportBooksToPdf()    → Génère PDF
└─ exportBooksToExcel()  → Génère XLSX

Détails:
├─ PDF: iText library
│  └─ Tableau avec colonnes: Titre, Auteur, ISBN, Stock, etc.
├─ Excel: Apache POI library
│  └─ Feuille "Books" avec toutes les données
└─ Limitations: Export limité aux 1000 premiers livres

Retour:
└─ byte[] du fichier à télécharger
```

---

## 📝 Exemples d'utilisation

### Exemple 1: Inscription et connexion

#### 1.1 Inscription

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'

Réponse 201:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTcwNTMxMjQ0MCwiZXhwIjoxNzA1MzEzMzQwfQ.pQ_y4x9kL...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZSIsImlhdCI6MTcwNTMxMjQ0MCwiZXhwIjoxNzA1OTE3MjQwfQ...",
    "tokenType": "Bearer",
    "username": "alice",
    "email": "alice@example.com"
  },
  "message": "Inscription réussie",
  "timestamp": "2024-01-15T10:34:00"
}

Alice a maintenant un compte ROLE_USER
```

#### 1.2 Connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "SecurePass123"
  }'

Réponse 200:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "username": "alice",
    "email": "alice@example.com"
  },
  "message": "Connexion réussie"
}

Le client stocke accessToken et refreshToken
```

---

### Exemple 2: Gestion des livres

#### 2.1 Créer un livre (Admin/Manager)

```bash
# Header: Authorization: Bearer {ACCESS_TOKEN}

curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "title": "Le Hobbit",
    "author": "J.R.R. Tolkien",
    "isbn": "978-2253045649",
    "description": "L'histoire de Bilbo Sacquet...",
    "stock": 8,
    "publishedDate": "1937-09-21"
  }'

Réponse 201:
{
  "success": true,
  "data": {
    "id": 42,
    "title": "Le Hobbit",
    "author": "J.R.R. Tolkien",
    "isbn": "978-2253045649",
    "description": "L'histoire de Bilbo Sacquet...",
    "stock": 8,
    "publishedDate": "1937-09-21",
    "categories": [],
    "createdAt": "2024-01-15T10:35:00"
  },
  "message": "Livre créé"
}
```

#### 2.2 Assigner des catégories

```bash
curl -X POST http://localhost:8080/api/books/42/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ..." \
  -d '[1, 3]'  # IDs des catégories: Fantasy et Adventure

Réponse 200:
{
  "success": true,
  "data": {
    "id": 42,
    "title": "Le Hobbit",
    "categories": [
      {"id": 1, "name": "Fantasy"},
      {"id": 3, "name": "Adventure"}
    ],
    ...
  },
  "message": "Catégories assignées"
}
```

#### 2.3 Rechercher des livres

```bash
# Rechercher avec pagination et filtrage
curl -X GET "http://localhost:8080/api/books?page=0&size=10&search=Hobbit&categoryId=1&sort=title&direction=asc" \
  -H "Authorization: Bearer ..."

Réponse 200:
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 42,
        "title": "Le Hobbit",
        ...
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0,
    "pageSize": 10
  }
}
```

---

### Exemple 3: Workflow d'emprunt

#### 3.1 Créer un emprunt

```bash
curl -X POST http://localhost:8080/api/loans \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ..." \
  -d '{
    "bookId": 42,
    "userId": 1,
    "loanDate": "2024-01-15",
    "expectedReturnDate": "2024-01-29"
  }'

Réponse 201:
{
  "success": true,
  "data": {
    "id": 101,
    "book": {
      "id": 42,
      "title": "Le Hobbit",
      "stock": 7  # Stock diminué!
    },
    "user": {
      "id": 1,
      "username": "alice"
    },
    "loanDate": "2024-01-15",
    "expectedReturnDate": "2024-01-29",
    "actualReturnDate": null,
    "status": "ACTIVE",
    "detail": {
      "id": 51,
      "notes": "",
      "renewalCount": 0,
      "returnedBy": null
    }
  },
  "message": "Emprunt créé"
}
```

**Changements en base de données:**
- Book.stock: 8 → 7
- Loan créé avec status ACTIVE
- LoanDetail créé

#### 3.2 Retourner l'emprunt

```bash
curl -X PUT http://localhost:8080/api/loans/101/return \
  -H "Authorization: Bearer ..." \
  -G --data-urlencode "returnedBy=Agent_Jean"

Réponse 200:
{
  "success": true,
  "data": {
    "id": 101,
    "book": {
      "id": 42,
      "title": "Le Hobbit",
      "stock": 8  # Stock restauré!
    },
    "status": "RETURNED",
    "actualReturnDate": "2024-01-27",
    "detail": {
      "id": 51,
      "itemcondition": "Bon état",
      "returnedBy": "Agent_Jean"
    }
  },
  "message": "Emprunt retourné"
}
```

**Changements en base de données:**
- Book.stock: 7 → 8
- Loan.status: ACTIVE → RETURNED
- Loan.actualReturnDate: null → 2024-01-27
- LoanDetail.returnedBy mis à jour

---

### Exemple 4: Voir mes emprunts

#### 4.1 Utilisateur consulte ses emprunts

```bash
curl -X GET "http://localhost:8080/api/loans/my?page=0&size=5" \
  -H "Authorization: Bearer {TOKEN_ALICE}"

Réponse 200:
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "book": {
          "id": 42,
          "title": "Le Hobbit"
        },
        "loanDate": "2024-01-15",
        "expectedReturnDate": "2024-01-29",
        "actualReturnDate": "2024-01-27",
        "status": "RETURNED"
      },
      {
        "id": 102,
        "book": {
          "id": 5,
          "title": "Le Seigneur des Anneaux"
        },
        "loanDate": "2024-01-20",
        "expectedReturnDate": "2024-02-03",
        "actualReturnDate": null,
        "status": "ACTIVE"
      }
    ],
    "totalElements": 2,
    "totalPages": 1
  },
  "message": "Mes emprunts récupérés"
}
```

---

### Exemple 5: Dashboard statistiques

```bash
curl -X GET http://localhost:8080/api/dashboard/stats \
  -H "Authorization: Bearer {TOKEN_MANAGER}"

Réponse 200:
{
  "success": true,
  "data": {
    "totalBooks": 150,
    "totalUsers": 45,
    "totalLoans": 542,
    "activeLoans": 28,
    "overdueLoans": 3,
    "returnedLoans": 511,
    "totalCategories": 12,
    "averageLoanDuration": 14,
    "booksByCategory": [
      {"category": "Fantasy", "count": 35},
      {"category": "Science-Fiction", "count": 28},
      {"category": "Mystery", "count": 22}
    ],
    "monthlyStats": [
      {"month": "January", "loans": 45, "returns": 42},
      {"month": "December", "loans": 38, "returns": 40}
    ],
    "topBooks": [
      {
        "id": 5,
        "title": "Le Seigneur des Anneaux",
        "borrowCount": 25
      }
    ]
  }
}
```

---

## 🔧 Configuration et déploiement

### Configuration application.yml

```yaml
spring:
  application:
    name: ensam-bibliotheque
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    # Exemple: jdbc:mysql://localhost:3306/ensam_bibliotheque?createDatabaseIfNotExist=true
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  sql:
    init:
      mode: always  # Exécute data.sql au démarrage
  
  jpa:
    defer-datasource-initialization: true
    hibernate:
      ddl-auto: update  # Crée/met à jour les tables automatiquement
    show-sql: true  # Affiche les requêtes SQL en logs
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  jackson:
    time-zone: UTC
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080

jwt:
  secret: ${spring.jwt.secret:default-secret-key}
  access-token-expiration: 900000  # 15 minutes en ms
  refresh-token-expiration: 604800000  # 7 jours en ms

cors:
  allowed-origins: http://localhost:5173
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Variables d'environnement requises

```bash
# Base de données MySQL
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ensam_bibliotheque
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root

# JWT
SPRING_JWT_SECRET=base64_encoded_secret_key_32_characters_minimum
```

### Structure des dossiers

```
Backned/
├── src/
│   ├── main/
│   │   ├── java/com/ensam/projet/
│   │   │   ├── ProjetApplication.java      # Point d'entrée Spring
│   │   │   ├── controller/                 # Controllers REST
│   │   │   ├── service/
│   │   │   │   ├── interfaces/             # Service contracts
│   │   │   │   └── impl/                   # Implémentations
│   │   │   ├── entity/                     # JPA entities
│   │   │   ├── repository/                 # Spring Data repos
│   │   │   ├── dto/
│   │   │   │   ├── request/                # DTOs pour requêtes
│   │   │   │   └── response/               # DTOs pour réponses
│   │   │   ├── security/                   # JWT et auth
│   │   │   ├── config/                     # Spring configs
│   │   │   ├── mapper/                     # MapStruct mappers
│   │   │   └── exception/                  # Exceptions custom
│   │   └── resources/
│   │       ├── application.yml             # Configuration principale
│   │       ├── application-dev.yml         # Config développement
│   │       ├── application-prod.yml        # Config production
│   │       └── data.sql                    # Données d'initialisation
│   └── test/
│       └── java/com/ensam/projet/          # Tests unitaires/intégration
├── pom.xml                                  # Maven dependencies
├── README.md                                # Documentation
└── Dockerfile                              # Pour containerisation
```

### Commandes Maven

```bash
# Compiler le projet
mvn clean compile

# Exécuter les tests
mvn test

# Packager en JAR
mvn clean package

# Lancer l'application
mvn spring-boot:run

# Lancer avec profil spécifique
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Générer la documentation Swagger
mvn clean package
# Puis accéder à: http://localhost:8080/swagger-ui.html
```

### Déploiement avec Docker

```dockerfile
# Dockerfile
FROM openjdk:17-slim
COPY target/bibliotheque-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
```

```bash
# Build Docker image
docker build -t ensam-bibliotheque:latest .

# Run Docker container
docker run -d \
  --name bibliotheque \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ensam_bibliotheque \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  ensam-bibliotheque:latest
```

### Docker Compose

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ensam_bibliotheque
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/ensam_bibliotheque
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql

volumes:
  mysql_data:
```

```bash
# Lancer avec Docker Compose
docker-compose up -d
```

---

## 📊 Résumé des dépendances et technologies

| Technologie | Version | Utilisation |
|------------|---------|------------|
| Java | 17 | Langage principal |
| Spring Boot | 3.2.2 | Framework web |
| Spring Security | 3.2.2 | Authentification/Autorisation |
| Spring Data JPA | 3.2.2 | ORM et accès données |
| MySQL | 8.0 | Base de données |
| JJWT | 0.11.5 | Tokens JWT |
| MapStruct | 1.5.5 | Mapping DTO/Entity |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI |
| iText | 7.2.5 | Génération PDF |
| Apache POI | 5.2.5 | Génération Excel |
| Lombok | - | Boilerplate reduction |
| JUnit 5 | 5.9.2 | Tests unitaires |
| Mockito | - | Mocking en tests |
| TestContainers | 1.17.6 | Tests d'intégration |
| MySQL Connector | 8.0 | Driver JDBC |

---

## 🎯 Points clés

### Avantages de cette architecture

1. **Séparation des préoccupations** → Couches bien définies (Controller → Service → Repository)
2. **Sécurité robuste** → JWT + Spring Security + BCrypt
3. **Extensibilité** → Facile d'ajouter de nouveaux endpoints/services
4. **Maintenabilité** → Code organisé et testable
5. **Performance** → Pagination, filtrage, caching potentiel
6. **Documentation** → Swagger auto-généré

### Flux standards

```
HTTP Request
    ↓
@RequestMapping intercepte
    ↓
JwtAuthFilter valide token
    ↓
@PreAuthorize vérifie rôles
    ↓
Controller reçoit la requête
    ↓
Service exécute logique métier
    ↓
Repository accède à la DB
    ↓
Entity est persistée/récupérée
    ↓
DTO retourné au client
    ↓
ApiResponse<T> wrappé
    ↓
JSON sérialisé
    ↓
HTTP Response
```

---

## 📌 Conclusion

Ce système de gestion de bibliothèque est une **application enterprise-grade** avec:
- Architecture en couches bien structurée
- Sécurité robuste avec JWT
- API RESTful complète et documentée
- Gestion complète des cas d'usage
- Support de l'export et des statistiques
- Facile à maintenir et à étendre

Pour toute question ou clarification, consultez la documentation Swagger: `http://localhost:8080/swagger-ui.html`

---

**Document généré le**: 15 Janvier 2024  
**Version du Backend**: 1.0.0  
**Version de Spring Boot**: 3.2.2  
**Java Version**: 17
