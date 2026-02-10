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
    
    public String createUserInFirebase(User user) throws Exception {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .setDisplayName(user.getNom());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        
        // Synchroniser aussi dans Firestore
        syncUserToFirestore(user, userRecord.getUid());

        return userRecord.getUid();
    }
    
    private void syncUserToFirestore(User user, String firebaseUid) throws ExecutionException, InterruptedException {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id_user", user.getId_user());
        userData.put("firebase_uid", firebaseUid);
        userData.put("email", user.getEmail());
        userData.put("nom", user.getNom());
        userData.put("prenom", user.getPrenom());
        userData.put("password", user.getPassword());
        userData.put("synced", true);
        userData.put("last_sync", new Date());
        userData.put("source", "postgres");
        
        // Attention: vérifiez que votre modèle User a bien ces méthodes
        if (user.getId_role() != null) {
            userData.put("id_role", user.getId_role());
        }
        
        db.collection("user_").document(firebaseUid).set(userData).get();
    }
    
    @Scheduled(fixedDelay = 60000) // toutes les 60s
    public void syncOfflineUsers() {
        if (!NetworkUtil.hasInternetConnection()) return;

        List<User> offlineUsers = userRepository.findBySyncedFalse();

        for (User u : offlineUsers) {
            try {
                String firebaseUid = FirebaseUtils.register(u.getEmail(), u.getPassword());
                u.setFirebaseUid(firebaseUid);
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
            syncSignalementsFromFirebase();
            syncUsersFromFirebase();
            
            System.out.println("✅ Synchronisation Firebase → PostgreSQL terminée");
        } catch (Exception e) {
            throw new Exception("Erreur synchronisation depuis Firebase: " + e.getMessage(), e);
        }
    }
    
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
    private void syncUsersToFirebase() throws ExecutionException, InterruptedException {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (user.getFirebaseUid() == null) {
                continue;
            }
            
            DocumentReference docRef = db.collection("user_").document(user.getFirebaseUid());
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id_user", user.getId_user());
            userData.put("firebase_uid", user.getFirebaseUid());
            userData.put("email", user.getEmail());
            userData.put("nom", user.getNom());
            userData.put("prenom", user.getPrenom());
            userData.put("password", user.getPassword());
            userData.put("synced", user.isSynced());
            userData.put("last_sync", new Date());
            userData.put("source", "postgres");
            
            if (user.getId_role() != null) {
                userData.put("id_role", user.getId_role());
            }
            
            docRef.set(userData).get();
        }
        
        System.out.println("✅ Utilisateurs synchronisés: " + users.size());
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
            sigData.put("user_firebase_uid", signalement.getUser().getFirebaseUid());
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
        if (data.containsKey("date_") && data.containsKey("id_user")) {
            Timestamp timestamp = (Timestamp) data.get("date_");
            Long userId = ((Number) data.get("id_user")).longValue();
            
            Date firebaseDate = timestamp.toDate();
            LocalDateTime date = LocalDateTime.ofInstant(firebaseDate.toInstant(), ZoneId.systemDefault());
            
            // Chercher un signalement avec la même date et utilisateur
            List<Signalement> similar = signalementRepository
                .findByDateAndUserId(date, userId);
            
            if (!similar.isEmpty()) {
                // Retourner le plus récent
                return similar.get(0);
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
        if (!document.contains("id_user")) {
            return false; // User manquant
        }
        Number userIdNum = (Number) data.get("id_user");
        if (userIdNum == null) {
            return false; // User null
        }
        Long userId = userIdNum.longValue();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.out.println("⚠️ Utilisateur non trouvé: " + userId);
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
        QuerySnapshot querySnapshot = db.collection("user_").get().get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        
        for (QueryDocumentSnapshot document : documents) {
            Map<String, Object> data = document.getData();
            
            String source = (String) data.getOrDefault("source", "unknown");
            
            if ("firebase_auth".equals(source)) {
                String email = (String) data.get("email");
                String firebaseUid = (String) data.get("firebase_uid");
                
                Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
                User user;
                
                if (existingUser.isPresent()) {
                    user = existingUser.get();
                } else {
                    user = new User();
                    user.setFirebaseUid(firebaseUid);
                }
                
                user.setEmail(email);
                user.setNom((String) data.get("nom"));
                user.setPrenom((String) data.get("prenom"));
                user.setSynced(true);
                
                userRepository.save(user);
                System.out.println("👤 Utilisateur synchronisé depuis Firebase: " + email);
                
                document.getReference().update("synced", true, "last_sync", new Date(), "source", "synced");
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
    
    // @Scheduled(fixedDelay = 60000) // Toutes les 60 secondes
    // public void autoSyncFromFirebase() {
    //     if (!NetworkUtil.hasInternetConnection()) {
    //         System.out.println("⚠️  Pas de connexion Internet, synchronisation différée");
    //         return;
    //     }
        
    //     try {
    //         syncAllFromFirebase();
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
}