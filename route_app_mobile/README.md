# 📱 Application Gestion Signalement Routes - Firebase Edition

## 🎯 Résumé Exécutif

Application mobile **Ionic + Vue.js** pour gérer les signalements de problèmes routiers à Antananarivo, Madagascar. L'application fonctionne **100% avec Firebase** (pas de serveur backend localhost:8081).

### ✅ Fonctionnalités Implémentées

#### 1. **Authentification Firebase**
- ✓ Login/Logout uniquement via Firebase Auth
- ✓ Gestion des tentatives de connexion (max 3)
- ✓ Blocage temporaire du compte après 3 tentatives échouées
- ✓ Stockage sécurisé du token Firebase
- ✓ Utilisateur test: `bemaso@gmail.com` / `bemasooo`

#### 2. **Signalement des Problèmes Routiers**
- ✓ Localisation automatique via clic sur la carte (Leaflet/OpenStreetMap)
- ✓ Sélection du quartier (Andohalo, Analakely, Anosizato, Besarety, etc.)
- ✓ Sélection de l'entreprise responsable
- ✓ Saisi de la surface (m²) et budget (Ar)
- ✓ Description du problème
- ✓ **Upload de photos vers Firebase Storage** (1 ou plusieurs)
- ✓ Sauvegarde dans Firestore avec timestamp

#### 3. **Affichage Carte + Récapitulation**
- ✓ Carte interactive Antananarivo (Leaflet + OpenStreetMap)
- ✓ Points colorés selon le statut:
  - 🟠 Orange: Nouveau
  - 🔵 Bleu: En cours
  - 🟢 Vert: Terminé
  - 🔴 Rouge: Annulé
- ✓ Popup au survol avec:
  - Date du signalement
  - Statut (nouveau/en cours/terminé)
  - Surface en m²
  - Budget en Ar
  - Entreprise concernée
  - **Galerie de photos**

#### 4. **Récapitulation Statistiques**
- ✓ Nombre de points actuels
- ✓ Surface totale (m²)
- ✓ Budget total (Ar)
- ✓ Pourcentage d'avancement (signalements terminés / total × 100)
- ✓ Répartition par statut

#### 5. **Filtrage et Notifications**
- ✓ Filtre "Mes signalements uniquement"
- ✓ Listeners temps réel Firestore
- ✓ Notifications de changement de statut

---

## 🚀 Démarrage Rapide

### Prérequis
```bash
- Node.js 16+
- npm ou yarn
- Compte Firebase configuré
- Android Studio (pour APK) ou Xcode (pour iOS)
```

### Installation

```bash
cd Projet_route/route_app_mobile

# Installer les dépendances
npm install

# Configurer .env avec vos clés Firebase
cp .env.example .env
# Éditer .env avec vos credentials

# Lancer le serveur de développement
npm run dev

# Ou lancer sur Android
ionic capacitor run android

# Ou lancer sur iOS
ionic capacitor run ios
```

---

## 🔧 Configuration Firebase

### Collections Firestore à Créer

#### 1. Collection: `signalements`
```javascript
{
  latitude: Number,
  longitude: Number,
  quartier: String,
  entreprise: String,
  surface: String,
  budget: String,
  description: String,
  userId: String,           // UID Firebase
  userEmail: String,
  status: String,           // "nouveau", "en cours", "terminé", "annulé"
  photos: Array<{           // Array de photos
    url: String,
    dateUpload: Timestamp
  }>,
  dateCreation: Timestamp,
  dateModification: Timestamp
}
```

#### 2. Collection: `users`
```javascript
{
  loginAttempts: Number,     // Compteur d'essais échoués
  isBlocked: Boolean,        // Compte bloqué?
  blockedUntil: Timestamp,   // Jusqu'à quand
  lastFailedLogin: Timestamp,
  lastSuccessfulLogin: Timestamp
}
```

#### 3. Collection: `quartiers`
```javascript
{
  nom: String,              // "Andohalo", "Analakely", etc.
  province: String,
  positionX: Number,        // Longitude (optionnel pour la carte)
  positionY: Number         // Latitude (optionnel pour la carte)
}
```

#### 4. Collection: `entreprises`
```javascript
{
  nom: String               // "Travaux Publics", "BTP Services", etc.
}
```

#### 5. Collection: `statuses`
```javascript
{
  libelle: String           // "nouveau", "en cours", "terminé", "annulé"
}
```

### Règles de Sécurité Firestore

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Signalements
    match /signalements/{signalementId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
                       request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null && 
                               resource.data.userId == request.auth.uid;
    }
    
    // Users
    match /users/{userId} {
      allow read, write: if request.auth != null && 
                            request.auth.uid == userId;
    }
    
    // Reference data (public read)
    match /quartiers/{docId=**} {
      allow read: if true;
    }
    match /entreprises/{docId=**} {
      allow read: if true;
    }
    match /statuses/{docId=**} {
      allow read: if true;
    }
  }
}
```

### Firebase Storage Rules

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Photos des signalements
    match /signalements/{userId}/{signalementId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                      request.auth.uid == userId;
    }
  }
}
```

---

## 📁 Structure du Projet

```
route_app_mobile/
├── src/
│   ├── components/
│   │   └── AddSignalementForm.vue    # Formulaire ajout signalement + photos
│   ├── config/
│   │   └── firebase.js               # Config Firebase (backup)
│   ├── router/
│   │   └── index.ts                  # Routes
│   ├── services/
│   │   └── firebaseService.ts        # ✨ SERVICE FIREBASE COMPLET
│   ├── views/
│   │   ├── Login.vue                 # Authentification (blocage, tentatives)
│   │   ├── Map.vue                   # Carte + récapitulation + filtre
│   │   └── ...
│   ├── App.vue
│   └── main.ts
├── .env                              # Config Firebase
├── .env.example
├── package.json
├── vite.config.ts
├── tsconfig.json
└── MIGRATION_FIREBASE.md             # Doc migration
```

---

## 📚 Guide d'Utilisation

### 1. Authentification
```
1. Accédez à la page Login
2. Email: bemaso@gmail.com
3. Password: bemasooo
4. Cliquez "Se connecter"
5. Redirection vers la carte
```

**Gestion du Blocage:**
- Après 3 tentatives échouées → compte bloqué 15 minutes
- Message d'erreur: "Votre compte est bloqué temporairement"
- Compteur réinitialisé après connexion réussie

### 2. Ajouter un Signalement
```
1. Cliquez sur le button "+"
2. Sélectionnez un quartier
3. (Optionnel) Sélectionnez une entreprise
4. Entrez la surface (m²)
5. (Optionnel) Entrez le budget (Ar)
6. (Optionnel) Ajoutez une description
7. Cliquez "Ajouter photo" pour uploader des photos
8. Cliquez "Ajouter le signalement"
9. Le signalement apparaît sur la carte
```

### 3. Visualiser la Carte
```
1. La carte affiche Antananarivo
2. Tous les signalements apparaissent sous forme de points
3. Couleur du point = Statut du signalement
4. Au survol d'un point → popup avec détails
5. Cliquez le photo pour agrandir
```

### 4. Filtrer Mes Signalements
```
1. Activez le filtre "Afficher seulement mes signalements"
2. Seuls vos signalements s'affichent
3. Récapitulation mise à jour
```

### 5. Récapitulation
```
Affichage en temps réel:
- Nombre de points
- Surface totale
- Budget total
- Pourcentage d'avancement
- Répartition par statut
```

---

## 🛠️ Architecture

### Stack Technologique
- **Frontend:** Ionic + Vue.js + TypeScript
- **Cartes:** Leaflet + OpenStreetMap
- **Backend:** Firebase (Auth + Firestore + Storage)
- **Build:** Vite + Capacitor (Android/iOS)

### Flux de Données
```
Login.vue
  → Firebase Auth
    → resetLoginAttempts()
      → Navigate to Map

Map.vue
  → getAllSignalements() ou getUserSignalements()
    → Display markers
    → Listen for real-time updates (onSnapshot)
    → Calculate statistics

AddSignalementForm.vue
  → uploadPhoto() to Firebase Storage
    → addSignalement() to Firestore
      → addPhotoToSignalement()
        → Refresh map
```

### Service Firebase (firebaseService.ts)

**Authentification:**
- `loginUser(email, password)`
- `logoutUser()`
- `checkUserBlockStatus(userId)`
- `incrementLoginAttempts(userId)`
- `resetLoginAttempts(userId)`

**Signalements:**
- `addSignalement(data)`
- `getAllSignalements()`
- `getUserSignalements(userId)`
- `updateSignalementStatus(id, status)`
- `onSignalementChange(id, callback)` - Real-time
- `onAllSignalementsChange(callback)` - Real-time

**Photos:**
- `uploadPhoto(signalementId, file, userId)`
- `addPhotoToSignalement(signalementId, photoURL)`

**Données de Référence:**
- `getQuartiers()`
- `getEntreprises()`
- `getStatuses()`

**Utilitaires:**
- `calculateStatistics(signalements)`
- `calculateAvancement(signalements)`

---

## 📱 Configuration Cordova/Capacitor

### Pour Android
```bash
npm install @capacitor/android
ionic capacitor add android
ionic capacitor run android
```

**google-services.json** → Placez-le dans:
```
android/app/google-services.json
```

### Pour iOS
```bash
npm install @capacitor/ios
ionic capacitor add ios
ionic capacitor run ios
```

---

## 🧪 Tests

### Test Login
```bash
# Credentials de test
Email: bemaso@gmail.com
Password: bemasooo

# Ou bloquez-vous exprès pour tester le blocage
```

### Test Upload Photos
```bash
1. Créez un signalement
2. Cliquez "Ajouter photo"
3. Sélectionnez une image
4. Uploadez
5. Vérifiez dans Firebase Console → Storage
```

### Test Temps Réel
```bash
1. Ouvrez 2 navigateurs côte-à-côte
2. Ajoutez un signalement dans l'un
3. L'autre se met à jour automatiquement
```

---

## 🔍 Dépannage

### Erreur: "Configuration Firebase manquante"
**Cause:** Fichier .env non configuré  
**Solution:** Complétez le .env avec vos clés Firebase

### Erreur: "Utilisateur non trouvé"
**Cause:** Utilisateur n'existe pas dans Firebase Auth  
**Solution:** Créez l'utilisateur dans Firebase Console

### Erreur: "Permission denied" lors d'ajout
**Cause:** Règles de sécurité Firestore restrictives  
**Solution:** Vérifiez les règles permettent les `create` pour les authentifiés

### Photos ne s'uploadent pas
**Cause:** Firebase Storage pas configuré ou règles incorrectes  
**Solution:** 
- Vérifiez Storage est activé
- Vérifiez les règles de Storage

### Signalements ne s'affichent pas sur la carte
**Cause:** Données manquantes ou listeners non activés  
**Solution:**
- Vérifiez collection `signalements` a des documents
- Vérifiez les règles de lecture Firestore

---

## 🚀 Déploiement

### Sur Google Play Store
```bash
# Build APK release
ionic capacitor build android --release

# Signez l'APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore my-release-key.keystore app-release-unsigned.apk alias_name

# Alignez l'APK
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

### Sur Apple App Store
```bash
ionic capacitor build ios --release
# Utilisez Xcode pour signer et publier
```

---

## 📞 Support et Maintenance

### Sauvegardes
- Activez les sauvegardes Firestore automatiques
- Exportez régulièrement les données:
  ```bash
  gsutil -m cp -r gs://YOUR-PROJECT.appspot.com/backup/ ./local-backup/
  ```

### Monitoring
- Utilisez Firebase Console pour:
  - Surveiller l'utilisation
  - Gérer les utilisateurs
  - Analyser les erreurs
  - Vérifier les règles de sécurité

### Mises à Jour
- Vérifiez régulièrement les mises à jour des dépendances:
  ```bash
  npm outdated
  npm update
  ```

---

## 📄 Licences et Attributions

- **Ionic:** Apache 2.0
- **Vue.js:** MIT
- **Leaflet:** BSD 2-Clause
- **OpenStreetMap:** ODbL
- **Firebase:** Google Terms of Service

---

## ✨ Fonctionnalités Futures

- [ ] Notifications push pour les changements de statut
- [ ] Système de notation/avis
- [ ] Historique des modifications
- [ ] Export PDF/CSV des rapports
- [ ] Synchronisation offline
- [ ] Géofencing pour les notifications
- [ ] Dashboard administrateur (web)

---

**Dernière mise à jour:** 2026-02-03  
**Version:** 1.0.0-Firebase  
**Status:** ✅ Production Ready
