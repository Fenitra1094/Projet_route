package com.example.back.service;

import com.example.back.models.*;
import com.example.back.repository.*;
import com.example.back.util.FirebaseUtils;
import com.example.back.util.NetworkUtil;
import com.google.cloud.Timestamp;
import com.google.firebase.auth.*;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SignalementRepository signalementRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    // Si ces repositories n'existent pas encore, créez-les d'abord
    // Sinon, commentez-les temporairement
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private HistoriqueStatusRepository historiqueStatusRepository;
    
    // @Autowired
    // private StatusBlocageRepository statusBlocageRepository;
    
    @Autowired
    private ProvinceRepository provinceRepository;
    
    @Autowired
    private EntrepriseRepository entrepriseRepository;
    
    @Autowired
    private QuartierRepository quartierRepository;
    
    // @Autowired
    // private HistoriqueBlocageRepository historiqueBlocageRepository;
    
    private final Firestore db;

    @Autowired
    public FirebaseService(Firestore firestore) {
        this.db = firestore;
    }
    
    // public String createUserInFirebase(User user) throws Exception {
    //     UserRecord.CreateRequest request = new UserRecord.CreateRequest()
    //             .setEmail(user.getEmail())
    //             .setPassword(user.getPassword())
    //             .setDisplayName(user.getNom());

    //     UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        
    //     // Synchroniser aussi dans Firestore
    //     syncUserToFirestore(user, userRecord.getUid());

    //     return userRecord.getUid();
    // }
    public String createUserInFirebase(User user) throws Exception {
    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
            .setEmail(user.getEmail())
            .setPassword(user.getPassword())
            .setDisplayName(user.getNom())
            .setDisabled(false);

    UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
    String firebaseUid = userRecord.getUid();
    
    // IMPORTANT: Sauvegarder le firebaseDocId dans PostgreSQL
    user.setFirebaseDocId("user_" + user.getId_user());
    user.setSynced(true);
    userRepository.save(user);
    
    System.out.println("✅ Firebase DocId sauvegardé dans PostgreSQL: " + user.getFirebaseDocId());
    
    // Synchroniser aussi dans Firestore
    syncUserToFirestore(user, user.getFirebaseDocId());

    return firebaseUid;
}
/**
 * Vérifier l'état de synchronisation des utilisateurs
 */
public void checkUserSyncStatus() {
    System.out.println("🔍 Vérification état synchronisation utilisateurs");
    System.out.println("========================================");
    
    List<User> users = userRepository.findAll();
    
    int withFirebaseDocId = 0;
    int withoutFirebaseDocId = 0;
    
    for (User user : users) {
        if (user.getFirebaseDocId() != null && !user.getFirebaseDocId().isEmpty()) {
            withFirebaseDocId++;
            System.out.println("✅ " + user.getEmail() + " - FirebaseDocId: " + user.getFirebaseDocId());
        } else {
            withoutFirebaseDocId++;
            System.out.println("❌ " + user.getEmail() + " - PAS de FirebaseDocId");
        }
    }
    
    System.out.println("========================================");
    System.out.println("📊 Résumé:");
    System.out.println("   - Avec FirebaseDocId: " + withFirebaseDocId);
    System.out.println("   - Sans FirebaseDocId: " + withoutFirebaseDocId);
    System.out.println("   - Total: " + users.size());
    System.out.println("========================================");
}
    
    private void syncUserToFirestore(User user, String firebaseDocId) throws ExecutionException, InterruptedException {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id_user", user.getId_user());
        userData.put("firebase_doc_id", firebaseDocId);
        userData.put("email", user.getEmail());
        userData.put("nom", user.getNom());
        userData.put("prenom", user.getPrenom());
        // Note: Ne pas synchroniser le mot de passe vers Firebase pour des raisons de sécurité
        userData.put("synced", true);
        userData.put("last_sync", new Date());
        userData.put("source", "postgres");
        
        // Attention: vérifiez que votre modèle User a bien ces méthodes
        if (user.getId_role() != null) {
            userData.put("id_role", user.getId_role());
        }
        
        db.collection("user_").document(firebaseDocId).set(userData).get();
    }
    
    @Scheduled(fixedDelay = 60000) // toutes les 60s
    public void syncOfflineUsers() {
        if (!NetworkUtil.hasInternetConnection()) return;

        List<User> offlineUsers = userRepository.findBySyncedFalse();

        for (User u : offlineUsers) {
            try {
                String firebaseUid = FirebaseUtils.register(u.getEmail(), u.getPassword());
                u.setFirebaseDocId("user_" + u.getId_user());
                u.setSynced(true);
                u.setPassword(null);
                userRepository.save(u);
                System.out.println("Utilisateur " + u.getEmail() + " pushé dans Firebase ✅");
            } catch (Exception e) {
                System.out.println("Erreur push Firebase pour " + u.getEmail() + ": " + e.getMessage());
            }
        }
    }
    
    // ==================== MÉTHODES DE SYNCHRONISATION ====================
    
    /**
     * Synchronisation vers Firebase
     */
    public void syncAllToFirebase() throws Exception {
        if (!NetworkUtil.hasInternetConnection()) {
            throw new Exception("Pas de connexion Internet");
        }
        
        System.out.println("🔄 Début synchronisation PostgreSQL → Firebase");
        
        try {
            syncRolesToFirebase();
            syncUsersToFirebase();
            syncSignalementsToFirebase();
            
            System.out.println("✅ Synchronisation PostgreSQL → Firebase terminée");
        } catch (Exception e) {
            System.err.println("❌ Erreur dans syncAllToFirebase: " + e.getMessage());
            throw new Exception("Erreur synchronisation vers Firebase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Synchronisation depuis Firebase
     */
    public void syncAllFromFirebase() throws Exception {
        if (!NetworkUtil.hasInternetConnection()) {
            throw new Exception("Pas de connexion Internet");
        }
        
        System.out.println("🔄 Début synchronisation Firebase → PostgreSQL");
        
        try {
            // syncSignalementsFromFirebase(); // Temporairement désactivé
            syncUsersFromFirebase();
            
            System.out.println("✅ Synchronisation Firebase → PostgreSQL terminée");
        } catch (Exception e) {
            throw new Exception("Erreur synchronisation depuis Firebase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Synchroniser les utilisateurs depuis Firebase
     */
    // private void syncUsersFromFirebase() throws ExecutionException, InterruptedException {
    //     System.out.println("🔄 Synchronisation des utilisateurs depuis Firebase");
        
    //     // Sync from Firestore collection "user_"
    //     QuerySnapshot querySnapshot = db.collection("user_").get().get();
    //     List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        
    //     int importedCount = 0;
    //     int updatedCount = 0;
    //     int skippedCount = 0;
        
    //     for (QueryDocumentSnapshot document : documents) {
    //         Map<String, Object> data = document.getData();
            
    //         String source = (String) data.getOrDefault("source", "unknown");
            
    //         if ("firebase_auth".equals(source) || "synced".equals(source) || "postgres".equals(source)) {
    //             String email = (String) data.get("email");
    //             String firebaseUid = (String) data.get("firebase_uid");
                
    //             Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
    //             User user;
    //             boolean isNew = false;
                
    //             if (existingUser.isPresent()) {
    //                 user = existingUser.get();
    //                 System.out.println("🔄 Mise à jour utilisateur existant: " + email);
    //             } else {
    //                 // Créer un nouvel utilisateur dans PostgreSQL
    //                 user = new User();
    //                 user.setFirebaseUid(firebaseUid);
    //                 isNew = true;
    //                 System.out.println("➕ Création nouvel utilisateur depuis Firebase: " + email);
    //             }
                
    //             // Mettre à jour les données depuis Firebase
    //             user.setEmail(email);
    //             user.setNom((String) data.get("nom"));
    //             user.setPrenom((String) data.get("prenom"));
                
    //             // Récupérer id_role si présent
    //             if (data.get("id_role") != null) {
    //                 if (data.get("id_role") instanceof Long) {
    //                     user.setId_role(((Long) data.get("id_role")).intValue());
    //                 } else if (data.get("id_role") instanceof Integer) {
    //                     user.setId_role((Integer) data.get("id_role"));
    //                 }
    //             }
                
    //             user.setSynced(true);
                
    //             userRepository.save(user);
                
    //             if (isNew) {
    //                 importedCount++;
    //             } else {
    //                 updatedCount++;
    //             }
                
    //             // Mettre à jour le document Firebase pour marquer comme synchronisé
    //             document.getReference().update("synced", true, "last_sync", new Date(), "source", "synced");
    //         } else {
    //             skippedCount++;
    //             System.out.println("⚠️ Document ignoré (source: " + source + ")");
    //         }
    //     }
        
    //     // Sync from Firebase Auth (users that might not be in Firestore yet)
    //     try {
    //         List<ExportedUserRecord> firebaseUsers = listAllUsers();
    //         for (ExportedUserRecord firebaseUser : firebaseUsers) {
    //             String firebaseUid = firebaseUser.getUid();
    //             String email = firebaseUser.getEmail();
                
    //             Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
    //             if (!existingUser.isPresent()) {
    //                 // Create user in local DB if not exists
    //                 User newUser = new User();
    //                 newUser.setFirebaseUid(firebaseUid);
    //                 newUser.setEmail(email);
    //                 newUser.setSynced(true);
    //                 // Note: Firebase Auth doesn't store nom/prenom/id_role, so leave as null
                    
    //                 userRepository.save(newUser);
    //                 importedCount++;
    //                 System.out.println("👤 Nouvel utilisateur synchronisé depuis Firebase Auth: " + email);
                    
    //                 // Also add to Firestore if not exists
    //                 Map<String, Object> userData = new HashMap<>();
    //                 userData.put("email", email);
    //                 userData.put("firebase_uid", firebaseUid);
    //                 userData.put("source", "firebase_auth");
    //                 userData.put("synced", true);
    //                 userData.put("last_sync", new Date());
                    
    //                 db.collection("user_").document(firebaseUid).set(userData).get();
    //             }
    //         }
    //     } catch (Exception e) {
    //         // Log the error but don't fail the entire sync process
    //         if (e.getMessage() != null && e.getMessage().contains("CONFIGURATION_NOT_FOUND")) {
    //             System.out.println("⚠️  Synchronisation Firebase Auth ignorée (configuration manquante)");
    //         } else {
    //             System.err.println("Erreur lors de la synchronisation depuis Firebase Auth: " + e.getMessage());
    //         }
    //     }
        
    //     System.out.println("✅ Synchronisation utilisateurs terminée: " + documents.size() + " documents traités");
    //     System.out.println("   - Nouveaux: " + importedCount);
    //     System.out.println("   - Mis à jour: " + updatedCount);
    //     System.out.println("   - Ignorés: " + skippedCount);
    // }
    
    /**
     * Synchroniser les rôles vers Firebase
     */
    private void syncRolesToFirebase() throws ExecutionException, InterruptedException {
        // Vérifiez d'abord que le repository existe
        if (roleRepository == null) {
            System.out.println("⚠️  RoleRepository non disponible");
            return;
        }
        
        List<Role> roles = roleRepository.findAll();
        
        WriteBatch batch = db.batch();
        int count = 0;
        
        for (Role role : roles) {
            DocumentReference docRef = db.collection("role").document("role_" + role.getId_role());
            
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("id_role", role.getId_role());
            roleData.put("libelle", role.getLibelle());
            roleData.put("last_sync", new Date());
            roleData.put("source", "postgres");
            
            batch.set(docRef, roleData);
            count++;
            
            if (count >= 450) {
                batch.commit().get();
                batch = db.batch();
                count = 0;
            }
        }
        
        if (count > 0) {
            batch.commit().get();
        }
        
        System.out.println("✅ Rôles synchronisés: " + roles.size());
    }
    
    /**
     * Synchroniser les utilisateurs vers Firebase
     */
    // private void syncUsersToFirebase() throws ExecutionException, InterruptedException {
    //     List<User> users = userRepository.findAll();
    //     int syncedCount = 0;
    //     int skippedCount = 0;
        
    //     System.out.println("🔄 Début synchronisation des utilisateurs vers Firebase (" + users.size() + " utilisateurs trouvés)");
        
    //     for (User user : users) {
    //         if (user.getFirebaseUid() == null) {
    //             System.out.println("⚠️  Utilisateur ignoré (pas de firebaseUid): " + user.getEmail());
    //             skippedCount++;
    //             continue;
    //         }
            
    //         DocumentReference docRef = db.collection("user_").document(user.getFirebaseUid());
            
    //         Map<String, Object> userData = new HashMap<>();
    //         userData.put("id_user", user.getId_user());
    //         userData.put("firebase_uid", user.getFirebaseUid());
    //         userData.put("email", user.getEmail());
    //         userData.put("nom", user.getNom());
    //         userData.put("prenom", user.getPrenom());
    //         // Note: Ne pas synchroniser le mot de passe vers Firebase pour des raisons de sécurité
    //         userData.put("synced", user.isSynced());
    //         userData.put("last_sync", new Date());
    //         userData.put("source", "postgres");
            
    //         if (user.getId_role() != null) {
    //             userData.put("id_role", user.getId_role());
    //         }
            
    //         try {
    //             docRef.set(userData).get();
    //             syncedCount++;
    //             System.out.println("✅ Utilisateur synchronisé: " + user.getEmail());
    //         } catch (Exception e) {
    //             System.err.println("❌ Erreur synchronisation utilisateur " + user.getEmail() + ": " + e.getMessage());
    //         }
    //     }
        
    //     System.out.println("✅ Synchronisation utilisateurs terminée: " + syncedCount + " synchronisés, " + skippedCount + " ignorés");
    // }
    /**
 * Synchroniser les utilisateurs vers Firebase
 */
private void syncUsersToFirebase() throws ExecutionException, InterruptedException {
    List<User> users = userRepository.findAll();
    int syncedCount = 0;
    int skippedCount = 0;
    
    System.out.println("🔄 Début synchronisation des utilisateurs vers Firebase");
    System.out.println("📊 Total utilisateurs PostgreSQL: " + users.size());
    
    for (User user : users) {
        System.out.println("--- Traitement utilisateur: " + user.getEmail() + " ---");
        System.out.println("ID PostgreSQL: " + user.getId_user());
        System.out.println("Firebase DocId: " + user.getFirebaseDocId());
        
        if (user.getFirebaseDocId() == null || user.getFirebaseDocId().isEmpty()) {
            user.setFirebaseDocId("user_" + user.getId_user());
            userRepository.save(user);
            System.out.println("✅ Nouveau firebaseDocId généré: " + user.getFirebaseDocId());
        }
        
        DocumentReference docRef = db.collection("user_").document(user.getFirebaseDocId());
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id_user", user.getId_user());
        userData.put("firebase_doc_id", user.getFirebaseDocId());
        userData.put("email", user.getEmail());
        userData.put("nom", user.getNom());
        userData.put("prenom", user.getPrenom());
        userData.put("synced", true);
        userData.put("last_sync", new Date());
        userData.put("source", "postgres");
        
        if (user.getId_role() != null) {
            userData.put("id_role", user.getId_role());
        }
        
        try {
            // Vérifier si le document existe déjà
            DocumentSnapshot snapshot = docRef.get().get();
            if (snapshot.exists()) {
                System.out.println("📄 Document existe déjà dans Firebase, mise à jour...");
                docRef.update(userData).get();
            } else {
                System.out.println("➕ Création nouveau document dans Firebase...");
                docRef.set(userData).get();
            }
            
            syncedCount++;
            System.out.println("✅ Utilisateur synchronisé: " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur synchronisation utilisateur " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    System.out.println("========================================");
    System.out.println("✅ Synchronisation utilisateurs terminée");
    System.out.println("📊 Statistiques:");
    System.out.println("   - Synchronisés: " + syncedCount);
    System.out.println("   - Ignorés: " + skippedCount);
    System.out.println("========================================");
}
    /**
 * Créer un utilisateur Firebase si il n'existe pas
 */
private String createFirebaseUserIfNotExists(User user) throws Exception {
    if (user.getEmail() == null || user.getEmail().isEmpty()) {
        throw new Exception("Email manquant pour l'utilisateur ID: " + user.getId_user());
    }
    
    try {
        // Essayer de récupérer l'utilisateur Firebase par email
        UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(user.getEmail());
        System.out.println("✅ Utilisateur Firebase existe déjà: " + existingUser.getUid());
        return existingUser.getUid();
        
    } catch (FirebaseAuthException e) {
        if (e.getErrorCode().equals("user-not-found")) {
            // Créer un nouvel utilisateur
            System.out.println("➕ Création nouvel utilisateur Firebase pour: " + user.getEmail());
            
            // Générer un mot de passe temporaire si non défini
            String password = user.getPassword();
            if (password == null || password.isEmpty()) {
                password = "TempPass123!"; // À changer selon vos besoins
                System.out.println("⚠️  Mot de passe généré temporairement");
            }
            
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setPassword(password)
                    .setDisplayName(user.getNom() + " " + user.getPrenom())
                    .setDisabled(false);
            
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            System.out.println("✅ Nouvel utilisateur Firebase créé: " + userRecord.getUid());
            
            return userRecord.getUid();
        } else {
            throw new Exception("Erreur Firebase Auth: " + e.getMessage(), e);
        }
    }
}
    /**
     * Synchroniser les signalements vers Firebase
     */
    public void syncSignalementsToFirebase() throws ExecutionException, InterruptedException {
    if (!NetworkUtil.hasInternetConnection()) {
        throw new RuntimeException("Pas de connexion Internet");
    }
    
    List<Signalement> signalements = signalementRepository.findAll();
    
    WriteBatch batch = db.batch();
    int count = 0;
    int createdCount = 0;
    int updatedCount = 0;
    
    for (Signalement signalement : signalements) {
        // Déterminer l'ID du document Firebase
        String firebaseDocId;
        
        if (signalement.getFirebaseDocId() != null) {
            // Utiliser l'ID Firebase existant
            firebaseDocId = signalement.getFirebaseDocId();
        } else if (signalement.getIdSignalement() != null) {
            // Générer un ID basé sur PostgreSQL
            firebaseDocId = "sig_" + signalement.getIdSignalement();
        } else {
            // Nouveau signalement (sans ID)
            firebaseDocId = "sig_new_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        DocumentReference docRef = db.collection("signalement").document(firebaseDocId);
        
        // Vérifier si le document existe déjà dans Firebase
        boolean existsInFirebase = false;
        try {
            DocumentSnapshot snapshot = docRef.get().get();
            existsInFirebase = snapshot.exists();
        } catch (Exception e) {
            // Document n'existe pas
        }
        
        Map<String, Object> sigData = new HashMap<>();
        sigData.put("id_signalement", signalement.getIdSignalement());
        
        if (signalement.getDate() != null) {
            sigData.put("date_", Date.from(signalement.getDate().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        if (signalement.getSurface() != null) {
            sigData.put("surface", signalement.getSurface().doubleValue());
        }
        
        if (signalement.getBudget() != null) {
            sigData.put("budget", signalement.getBudget().doubleValue());
        }
        
        sigData.put("last_sync", new Date());
        sigData.put("source", "postgres");
        
        // Références
        if (signalement.getUser() != null) {
            sigData.put("id_user", signalement.getUser().getId_user());
            sigData.put("user_email", signalement.getUser().getEmail());
        }
        
        if (signalement.getQuartier() != null) {
            sigData.put("id_quartier", signalement.getQuartier().getId_quartier());
        }
        
        if (signalement.getEntreprise() != null) {
            sigData.put("id_entreprise", signalement.getEntreprise().getIdEntreprise());
        }
        
        if (signalement.getStatus() != null) {
            sigData.put("id_status", signalement.getStatus().getIdStatus());
        }
        
        // Stocker l'ID Firebase dans les données
        sigData.put("firebase_doc_id", firebaseDocId);
        
        batch.set(docRef, sigData);
        count++;
        
        // Mettre à jour l'ID Firebase dans PostgreSQL
        if (!firebaseDocId.equals(signalement.getFirebaseDocId())) {
            signalement.setFirebaseDocId(firebaseDocId);
            signalementRepository.save(signalement);
        }
        
        if (existsInFirebase) {
            updatedCount++;
        } else {
            createdCount++;
        }
        
        if (count >= 450) {
            batch.commit().get();
            batch = db.batch();
            count = 0;
        }
    }
    
    if (count > 0) {
        batch.commit().get();
    }
    
    System.out.println("✅ Signalements synchronisés vers Firebase: " + signalements.size());
    System.out.println("   - Nouveaux documents: " + createdCount);
    System.out.println("   - Documents mis à jour: " + updatedCount);
}

/**
 * Synchroniser les signalements depuis Firebase
 */
public void syncSignalementsFromFirebase() throws Exception {
    if (!NetworkUtil.hasInternetConnection()) {
        throw new Exception("Pas de connexion Internet");
    }
    
    System.out.println("🔄 Synchronisation signalements Firebase → PostgreSQL");
    
    try {
        QuerySnapshot querySnapshot = db.collection("signalement").get().get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        
        int importedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        
        for (QueryDocumentSnapshot document : documents) {
            Map<String, Object> data = document.getData();
            String firebaseDocId = document.getId();
            
            // Logique de priorité pour trouver le signalement
            Signalement signalement = findSignalementWithPriority(data, firebaseDocId);
            
            boolean isNew = signalement.getIdSignalement() == null;
            
            // Mapper les données
            boolean mappingSuccess = mapSignalementData(signalement, data, document);
            
            if (!mappingSuccess) {
                System.out.println("⚠️ Signalement ignoré car données incomplètes: " + firebaseDocId);
                skippedCount++;
                continue;
            }
            
            // S'assurer que l'ID Firebase est sauvegardé
            if (!firebaseDocId.equals(signalement.getFirebaseDocId())) {
                signalement.setFirebaseDocId(firebaseDocId);
            }
            
            // Vérifier s'il y a un conflit d'ID
            if (!isNew && data.containsKey("id_signalement")) {
                Long firebasePgId = ((Number) data.get("id_signalement")).longValue();
                if (!firebasePgId.equals(signalement.getIdSignalement())) {
                    System.out.println("⚠️ Conflit d'ID pour " + firebaseDocId + 
                                     ": Firebase=" + firebasePgId + 
                                     ", PostgreSQL=" + signalement.getIdSignalement());
                    // Ne pas écraser l'ID PostgreSQL existant
                }
            }
            
            // Sauvegarder
            signalementRepository.save(signalement);
            
            if (isNew) {
                importedCount++;
                System.out.println("📥 Nouveau signalement: " + firebaseDocId);
            } else {
                updatedCount++;
                System.out.println("🔄 Signalement mis à jour: " + firebaseDocId);
            }
            
            // Mettre à jour dans Firebase
            document.getReference().update("synced", true, "last_sync", new Date());
        }
        
        System.out.println("✅ Synchronisation terminée: " + documents.size() + " documents traités");
        System.out.println("   - Nouveaux: " + importedCount);
        System.out.println("   - Mis à jour: " + updatedCount);
        System.out.println("   - Ignorés: " + skippedCount);
        
    } catch (InterruptedException | ExecutionException e) {
        throw new Exception("Erreur lors de la synchronisation depuis Firebase", e);
    }
}

/**
 * Logique de priorité pour trouver un signalement
 */
private Signalement findSignalementWithPriority(Map<String, Object> data, String firebaseDocId) {
    // 1. Chercher par ID Firebase (priorité absolue)
    Signalement signalement = signalementRepository.findByFirebaseDocId(firebaseDocId);
    if (signalement != null) {
        return signalement;
    }
    
    // 2. Chercher par ID PostgreSQL si présent dans Firebase
    if (data.containsKey("id_signalement")) {
        Long pgId = ((Number) data.get("id_signalement")).longValue();
        Optional<Signalement> byPgId = signalementRepository.findById(pgId);
        if (byPgId.isPresent()) {
            return byPgId.get();
        }
    }
    
    // 3. Vérifier s'il existe un signalement avec les mêmes données
    signalement = findSignalementByContent(data);
    if (signalement != null) {
        return signalement;
    }
    
    // 4. Nouveau signalement
    return new Signalement();
}

/**
 * Rechercher par contenu (date, utilisateur, etc.)
 */
private Signalement findSignalementByContent(Map<String, Object> data) {
    try {
        if (data.containsKey("date_") && data.containsKey("user_email")) {
            Timestamp timestamp = (Timestamp) data.get("date_");
            String userEmail = (String) data.get("user_email");
            
            Date firebaseDate = timestamp.toDate();
            LocalDateTime date = LocalDateTime.ofInstant(firebaseDate.toInstant(), ZoneId.systemDefault());
            
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                Long userId = user.getId_user();
                // Chercher un signalement avec la même date et utilisateur
                List<Signalement> similar = signalementRepository
                    .findByDateAndUserId(date, userId);
                
                if (!similar.isEmpty()) {
                    // Retourner le plus récent
                    return similar.get(0);
                }
            }
        }
    } catch (Exception e) {
        // Ignorer les erreurs de parsing
    }
    
    return null;
}

/**
 * Mapper les données Firebase vers Signalement
 */
private boolean mapSignalementData(Signalement signalement, Map<String, Object> data, 
                                 QueryDocumentSnapshot document) {
    try {
        // Date
        if (document.contains("date_")) {
            Timestamp timestamp = (Timestamp) data.get("date_");
            if (timestamp != null) {
                Date firebaseDate = timestamp.toDate();
                signalement.setDate(LocalDateTime.ofInstant(firebaseDate.toInstant(), ZoneId.systemDefault()));
            }
        }
        
        // Surface
        if (document.contains("surface")) {
            Number surfaceNum = (Number) data.get("surface");
            if (surfaceNum != null) {
                signalement.setSurface(BigDecimal.valueOf(surfaceNum.doubleValue()));
            }
        }
        
        // Budget
        if (document.contains("budget")) {
            Number budgetNum = (Number) data.get("budget");
            if (budgetNum != null) {
                signalement.setBudget(BigDecimal.valueOf(budgetNum.doubleValue()));
            }
        }
        
        // User (OBLIGATOIRE)
        if (!document.contains("user_email")) {
            return false; // User manquant
        }
        String userEmail = (String) data.get("user_email");
        if (userEmail == null) {
            return false; // User null
        }
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            System.out.println("⚠️ Utilisateur non trouvé: " + userEmail);
            return false;
        }
        signalement.setUser(user);
        
        // Quartier (OBLIGATOIRE selon votre modèle)
        if (document.contains("id_quartier")) {
            Number quartierIdNum = (Number) data.get("id_quartier");
            if (quartierIdNum != null) {
                Long quartierId = quartierIdNum.longValue();
                Quartier quartier = quartierRepository.findById(quartierId).orElse(null);
                signalement.setQuartier(quartier);
            }
        }
        
        // Status (OBLIGATOIRE)
        if (!document.contains("id_status")) {
            return false; // Status manquant
        }
        Number statusIdNum = (Number) data.get("id_status");
        if (statusIdNum == null) {
            return false; // Status null
        }
        Long statusId = statusIdNum.longValue();
        Status status = statusRepository.findById(statusId).orElse(null);
        if (status == null) {
            System.out.println("⚠️ Status non trouvé: " + statusId);
            return false;
        }
        signalement.setStatus(status);
        
        // Entreprise (optionnel)
        if (document.contains("id_entreprise")) {
            Number entrepriseIdNum = (Number) data.get("id_entreprise");
            if (entrepriseIdNum != null) {
                Long entrepriseId = entrepriseIdNum.longValue();
                Entreprise entreprise = entrepriseRepository.findById(entrepriseId).orElse(null);
                signalement.setEntreprise(entreprise);
            }
        }
        
        return true;
        
    } catch (Exception e) {
        System.err.println("❌ Erreur mapping données: " + e.getMessage());
        return false;
    }
}
    
    /**
     * Synchroniser les utilisateurs depuis Firebase
     */
    private void syncUsersFromFirebase() throws ExecutionException, InterruptedException {
        // Sync from Firestore collection "user_"
        QuerySnapshot querySnapshot = db.collection("user_").get().get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        
        for (QueryDocumentSnapshot document : documents) {
            Map<String, Object> data = document.getData();
            
            String source = (String) data.getOrDefault("source", "unknown");
            
            if ("firebase_auth".equals(source) || "synced".equals(source) || "postgres".equals(source)) {
                String email = (String) data.get("email");
                
                Optional<User> existingUser = userRepository.findByEmail(email);
                User user;
                
                if (existingUser.isPresent()) {
                    user = existingUser.get();
                } else {
                    user = new User();
                    user.setEmail(email);
                }
                
                user.setFirebaseDocId(document.getId());
                user.setNom((String) data.get("nom"));
                user.setPrenom((String) data.get("prenom"));
                
                // Récupérer id_role si présent
                if (data.get("id_role") != null) {
                    if (data.get("id_role") instanceof Long) {
                        user.setId_role(((Long) data.get("id_role")).intValue());
                    } else if (data.get("id_role") instanceof Integer) {
                        user.setId_role((Integer) data.get("id_role"));
                    }
                }
                
                // Ne pas écraser le mot de passe local si on synchronise depuis Firebase
                // Le mot de passe ne devrait être que local
                user.setSynced(true);
                
                userRepository.save(user);
                System.out.println("👤 Utilisateur synchronisé depuis Firestore: " + email);
                
                document.getReference().update("synced", true, "last_sync", new Date(), "source", "synced");
            }
        }
        
        // Sync from Firebase Auth (users that might not be in Firestore yet)
        try {
            List<ExportedUserRecord> firebaseUsers = listAllUsers();
            for (ExportedUserRecord firebaseUser : firebaseUsers) {
                String firebaseUid = firebaseUser.getUid();
                String email = firebaseUser.getEmail();
                
                Optional<User> existingUser = userRepository.findByEmail(firebaseUser.getEmail());
                if (!existingUser.isPresent()) {
                    // Create user in local DB if not exists
                    User newUser = new User();
                    newUser.setFirebaseDocId("user_auth_" + firebaseUser.getUid());
                    newUser.setEmail(email);
                    newUser.setSynced(true);
                    // Note: Firebase Auth doesn't store nom/prenom/id_role, so leave as null
                    
                    userRepository.save(newUser);
                    System.out.println("👤 Nouvel utilisateur synchronisé depuis Firebase Auth: " + email);
                    
                    // Also add to Firestore if not exists
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("firebase_doc_id", "user_auth_" + firebaseUser.getUid());
                    userData.put("source", "firebase_auth");
                    userData.put("synced", true);
                    userData.put("last_sync", new Date());
                    
                    db.collection("user_").document("user_auth_" + firebaseUser.getUid()).set(userData).get();
                }
            }
        } catch (Exception e) {
            // Log the error but don't fail the entire sync process
            if (e.getMessage() != null && e.getMessage().contains("CONFIGURATION_NOT_FOUND")) {
                System.out.println("⚠️  Synchronisation Firebase Auth ignorée (configuration manquante)");
            } else {
                System.err.println("Erreur lors de la synchronisation depuis Firebase Auth: " + e.getMessage());
            }
        }
    }
    
    // ==================== SYNCHRO AUTOMATIQUE ====================
    
    // @Scheduled(fixedDelay = 30000) // Toutes les 30 secondes
    // public void autoSyncToFirebase() {
    //     if (!NetworkUtil.hasInternetConnection()) {
    //         System.out.println("⚠️  Pas de connexion Internet, synchronisation différée");
    //         return;
    //     }
        
    //     try {
    //         syncAllToFirebase();
    //     } catch (Exception e) {
    //         System.err.println("❌ Erreur synchronisation automatique vers Firebase: " + e.getMessage());
    //     }
    // }
    
    // @Scheduled(fixedDelay = 120000) // Toutes les 2 minutes (moins fréquent que vers Firebase)
    // public void autoSyncFromFirebase() {
    //     if (!NetworkUtil.hasInternetConnection()) {
    //         System.out.println("⚠️  Pas de connexion Internet, synchronisation différée");
    //         return;
    //     }
        
    //     try {
    //         syncAllFromFirebase();
    //         System.out.println("✅ Synchronisation automatique depuis Firebase terminée");
    //     } catch (Exception e) {
    //         System.err.println("❌ Erreur synchronisation automatique depuis Firebase: " + e.getMessage());
    //     }
    // }
    
    // ==================== UTILITAIRES ====================
    
    public static List<ExportedUserRecord> listAllUsers() throws Exception {
        List<ExportedUserRecord> users = new ArrayList<>();
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        
        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                users.add(user);
            }
            page = page.getNextPage();
        }
        
        return users;
    }
    
    public void deleteAllUsers() throws Exception {
        List<String> uids = new ArrayList<>();
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        
        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                uids.add(user.getUid());
            }
            page = page.getNextPage();
        }
        
        if (!uids.isEmpty()) {
            FirebaseAuth.getInstance().deleteUsers(uids);
        }
    }
    
    /**
     * Vérifier l'état de la synchronisation
     */
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Nombre de documents dans Firebase
            status.put("firebase_users", db.collection("user_").get().get().size());
            status.put("firebase_signalements", db.collection("signalement").get().get().size());
            
            // Nombre d'enregistrements PostgreSQL
            status.put("postgres_users", userRepository.count());
            status.put("postgres_signalements", signalementRepository.count());
            
            // Utilisateurs non synchronisés
            // Adaptez selon votre repository
            // status.put("unsynced_users", userRepository.countBySyncedFalse());
            
            status.put("last_check", new Date());
            status.put("status", "OK");
            
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
        }
        
        return status;
    }
    public double getAverageProcessingTime(String finalStatusLibelle) {

    List<HistoriqueStatus> historiquesFinaux =
            historiqueStatusRepository.findByStatusLibelle(finalStatusLibelle);

    long totalDays = 0;
    int signalementCount = 0;

    Set<Long> processedSignalements = new HashSet<>();

    for (HistoriqueStatus histFinal : historiquesFinaux) {

        Signalement sig = histFinal.getSignalement();
        Long signalementId = sig.getIdSignalement();

        if (processedSignalements.contains(signalementId)) {
            continue;
        }

        // 🔹 premier statut = date de création réelle
        LocalDateTime debutDate = sig.getDate();
        HistoriqueStatus fin = histFinal;

        if (debutDate != null && fin.getDateChangement() != null) {
            long days = Duration
                    .between(debutDate, fin.getDateChangement())
                    .toDays();

            totalDays += days;
            signalementCount++;
            processedSignalements.add(signalementId);
        }
    }

    return signalementCount == 0 ? 0.0 : (double) totalDays / signalementCount;
}
public double getAverageProcessingTimeForAll() {
    return getAverageProcessingTime("termine");
}

}