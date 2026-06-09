# 🌐 Bibliothèque ENSAM — Guide Complet du Frontend 

Bienvenue dans la documentation complète et détaillée du **Frontend** de l'application de gestion de la bibliothèque ENSAM. Ce document couvre absolument tout ce qui se trouve dans le frontend : l'architecture, les composants, les pages, le routage, la communication backend, et la gestion de l'état.

Ce projet est une Single Page Application (SPA) robuste développée avec **React 19**, **TypeScript** et **Vite**.

---

## 🛠️ La Stack Technologique Complète (package.json)

Le projet repose sur un ensemble d'outils modernes pour garantir la performance et la maintenabilité :

### Cœur et Build
*   **React (`^19.2.0`) & React DOM** : La bibliothèque principale pour la création de l'interface utilisateur.
*   **TypeScript (`^5.8.3`)** : Pour un typage strict, évitant les erreurs d'exécution.
*   **Vite (`^7.3.1`)** : Bundler ultra-rapide utilisé pour le développement local et le build de production.

### Routage et État
*   **TanStack Router (`^1.168.25`)** : Système de routage type-safe. Les routes sont générées automatiquement à partir de la structure des fichiers.
*   **TanStack Query (`^5.83.0`)** : Gestion du cache et des appels asynchrones (remplace Redux pour la gestion d'état serveur).
*   **Zustand (`^5.0.14`)** : Gestion d'état global léger (si nécessaire pour des états purement UI).

### Interface Utilisateur (UI) et Styling
*   **Material UI / MUI (`^9.0.1`)** : Utilisé pour les composants lourds comme `DataGrid` (tableaux avancés) et `DatePicker`.
*   **Tailwind CSS (`^4.2.1`)** : Framework CSS utilitaire pour le design global, la mise en page rapide et le mode responsive.
*   **Radix UI** : Primitives UI non stylisées (utilisées sous le capot pour des composants très accessibles comme les modales, menus, switchs).
*   **Recharts (`^3.8.1`)** : Utilisé pour générer les graphiques interactifs (Camemberts, Barres) sur le tableau de bord (Dashboard).
*   **Lucide React (`^0.575.0`)** & **MUI Icons** : Bibliothèques d'icônes.

### Outils Spécifiques
*   **Axios (`^1.16.1`)** : Pour les requêtes HTTP REST.
*   **SockJS (`^1.6.1`) & STOMP (`^7.3.0`)** : Pour la connexion WebSocket en temps réel.
*   **react-hook-form (`^7.71.2`) & Zod (`^3.24.2`)** : Pour la création de formulaires robustes avec validation de schéma.
*   **i18next (`^26.3.0`) & react-i18next** : Pour l'internationalisation (traduction bilingue).
*   **Sonner (`^2.0.7`)** : Pour l'affichage des notifications "Toasts".

---

## 🏗️ Architecture des Dossiers (`src/`)

L'application suit une structure modulaire stricte :

```text
frontend/src/
├── components/          # Composants UI partagés
├── context/             # Contextes globaux (AuthContext)
├── hooks/               # Custom hooks React
├── lib/                 # Configurations librairies (i18n, gestion des erreurs)
├── pages/               # Composants "Vues" correspondants aux URLs
├── routes/              # Fichiers de configuration de TanStack Router
├── services/            # Appels API (Axios) et WebSockets
├── utils/               # Helpers et fonctions utilitaires
├── app.tsx              # Racine de l'application React
├── router.tsx           # Configuration globale de TanStack Router
└── styles.css           # Styles globaux (Import Tailwind)
```

---

## 🗺️ Le Routage (TanStack Router)

Le routage est géré par **TanStack Router**. Le fichier `src/routeTree.gen.ts` est autogénéré.

### Arborescence des Routes

1.  **`/` (IndexRoute)** : Page d'accueil publique.
2.  **`/login` & `/register`** : Pages publiques pour l'authentification.
3.  **`/_app` (AppLayout)** : **Layout Protégé**. Toutes les routes enfants nécessitent une connexion valide.
    *   `/_app/dashboard` : Tableau de bord des statistiques (Admin/Manager).
    *   `/_app/users` : Gestion des utilisateurs (Admin).
    *   `/_app/categories` : Gestion des catégories (Admin/Manager).
    *   `/_app/books/` : Catalogue de livres complet (Tous les utilisateurs).
    *   `/_app/books/new` : Création d'un livre (Admin/Manager).
    *   `/_app/books/$bookId` : Modification d'un livre spécifique (Admin/Manager).
    *   `/_app/loans/` : Gestion globale des emprunts (Admin/Manager).
    *   `/_app/loans/my` : Emprunts personnels de l'utilisateur connecté (Tous les utilisateurs).
    *   `/_app/profile` : Gestion du profil (Tous les utilisateurs).

---

## 🖼️ Les Composants et Pages (Views)

### Pages Principales (`src/pages/`)
*   **`AdminDashboard.tsx` & `UserDashboard.tsx`** : Affiche les statistiques via `Recharts` (livres empruntés, retards). Le contenu diffère selon le rôle.
*   **`BooksPage.tsx`** : Affiche la liste des livres. Utilise `DataGrid` de MUI pour permettre la recherche, le tri, et la pagination. Affiche les boutons "Emprunter" (avec une logique pour bloquer si le stock est épuisé ou si le livre est déjà emprunté par l'utilisateur).
*   **`CategoriesPage.tsx`** : Gère la création et l'assignation de catégories aux livres.
*   **`LoansPage.tsx`** : Permet aux admins de voir tous les emprunts et de forcer un retour.
*   **`MyLoansPage.tsx`** : Permet aux étudiants/utilisateurs de consulter leurs propres dates de retours prévues.
*   **`ProfilePage.tsx`** : Permet à l'utilisateur de modifier son mot de passe ou ses informations (`react-hook-form` + `Zod`).

### Composants Partagés (`src/components/`)
*   **`AppLayout.tsx`** : Le layout principal de l'application connectée (Sidebar / Navbar / Contenu central).
*   **`BookForm.tsx`** : Formulaire réutilisable (pour l'ajout et l'édition) gérant les champs du livre.
*   **`ConfirmDialog.tsx`** : Modale réutilisable pour demander confirmation avant une suppression (Livre, Utilisateur).
*   **`NotificationManager.tsx`** : Composant invisible qui écoute les événements WebSocket et déclenche les Toasts `Sonner`.
*   **`Footer.tsx` & `PageHeader.tsx`** : Éléments de structure de page.

---

## 🔐 Authentification et Sécurité

Le système utilise un **AuthContext** (`src/context/auth.tsx`).

1.  **State Local** : Conserve l'utilisateur connecté, son rôle, et l'état de chargement.
2.  **Protection** : Le fichier `ProtectedRoute.tsx` enveloppe le layout principal. Si l'utilisateur n'est pas connecté, il est renvoyé vers `/login`. S'il essaie d'accéder à `/users` sans être Admin, il est bloqué.
3.  **`tokenStore`** : Un helper utilitaire qui écrit et lit l'AccessToken et le RefreshToken depuis le `localStorage` de manière sécurisée.

---

## 📡 Communication Backend 

### 1. Requêtes HTTP (Axios + JWT Interceptors)
Le fichier `src/services/http.ts` est la tour de contrôle des API.
*   **Intercepteur de Requête** : Ajoute `Authorization: Bearer <token>` à chaque appel.
*   **Intercepteur de Réponse (Le Refresh Token)** : Si le serveur répond `401 Unauthorized` (Token expiré) :
    1. L'intercepteur met la requête en pause.
    2. Appelle l'API `/api/auth/refresh` silencieusement.
    3. Met à jour le `localStorage`.
    4. Rejoue la requête originale. (L'utilisateur ne s'en rend même pas compte).

### 2. Le Temps Réel (WebSockets avec STOMP)
L'application doit réagir instantanément (ex: un autre étudiant emprunte le dernier livre, le bouton doit disparaître).
*   **Fichier** : `src/services/websocket.ts`
*   **Mécanique** : 
    1. Connexion via `SockJS` à `/ws-endpoint`.
    2. Utilisation de `@stomp/stompjs` pour s'abonner au canal `/topic/notifications`.
    3. Lorsque le backend Spring Boot émet un message (ex: "Nouvel emprunt validé"), le payload JSON est reçu.
    4. Le `NotificationManager` l'intercepte et affiche une alerte verte/bleue en bas de l'écran.

### 3. TanStack Query (Le Cache)
Presque tous les appels API utilisent des hooks comme `useQuery` ou `useMutation`.
*   **Exemple** : Quand on emprunte un livre (`useMutation`), on invalide le cache des livres (`queryClient.invalidateQueries({ queryKey: ['books'] })`). La grille `DataGrid` de MUI se met à jour instantanément sans recharger la page entière.

---

## 🌍 Internationalisation (i18n)

Le système gère plusieurs langues de façon fluide sans alourdir le bundle initial.

### 1. La Configuration (`src/lib/i18n.ts`)
*   **`i18next`** : Le cœur du moteur de traduction.
*   **`i18next-http-backend`** : Au lieu de compiler tous les textes dans le code JS, ce plugin télécharge les fichiers `translation.json` depuis le dossier public (`/locales/fr/translation.json`) de façon asynchrone ("Lazy Loading").
*   **`LanguageDetector`** : Regarde la langue du navigateur ou le `localStorage` pour choisir entre l'Anglais et le Français.

### 2. Les Fichiers de Traduction
Situés dans `public/locales/fr/translation.json` et `public/locales/en/translation.json`.
Exemple :
```json
{
  "sidebar": {
    "dashboard": "Tableau de Bord",
    "books": "Catalogue"
  }
}
```

### 3. L'Utilisation dans les composants
```tsx
import { useTranslation } from "react-i18next";

function Sidebar() {
  const { t, i18n } = useTranslation();
  
  // Pour traduire :
  return <span>{t("sidebar.dashboard")}</span>;

  // Pour changer de langue à chaud :
  // i18n.changeLanguage('en');
}
```

---

## 🐳 Docker et Variables d'Environnement

Le frontend s'intègre parfaitement dans Docker.
*   **Dev Mode** : Le backend est appelé via un proxy Vite pour éviter les erreurs CORS.
*   **Prod Mode** : Le build (`npm run build`) génère un dossier `/dist` contenant uniquement des fichiers statiques (HTML, CSS, JS minifiés) qui sont généralement servis par un serveur **Nginx** dans le conteneur Docker.
*   **Variable clé** : `VITE_API_BASE_URL` (définie dans `.env`) indique où se trouve le serveur Spring Boot.

---

## 📝 Formulaires et Validation

La création de livres, catégories, et l'authentification utilisent une approche moderne :
1.  **Zod** : Crée un "Schéma" de validation stricte (Exemple : *Le titre est obligatoire, le stock doit être > 0*).
2.  **React-Hook-Form** : S'occupe de gérer l'état des inputs sans provoquer de re-rendu complet de la page à chaque frappe au clavier.
3.  **Resolvers** : Connecte `Zod` à `React-Hook-Form` pour afficher automatiquement des messages d'erreur stylisés sous les champs fautifs.

---

**Ce frontend est donc une SPA d'entreprise complète, typée de bout en bout, avec une gestion avancée du cache, de la sécurité JWT, des connexions WebSocket, et une interface multilingue très riche.**
