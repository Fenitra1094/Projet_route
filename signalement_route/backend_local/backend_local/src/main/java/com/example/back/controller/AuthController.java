package com.example.back.controller;
import com.example.back.models.User;
import com.example.back.repository.UserRepository;
import com.example.back.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import com.example.back.util.*;
import com.google.firebase.auth.ExportedUserRecord;
import com.example.back.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
@Tag(name = "Authentification", description = "API d'authentification et de gestion des utilisateurs")
public class AuthController {

    // Constants for status blocage
    private static final Integer STATUS_BLOQUE = 2;
    private static final Integer STATUS_ACTIF = 1;

    private final UserRepository userRepository;
    private FirebaseUtils firebaseUtils;
    private FirebaseService firebaseUserService;

    
    public AuthController(
            UserRepository userRepository,
            FirebaseService firebaseUserService
    ) {
        this.userRepository = userRepository;
        this.firebaseUserService = firebaseUserService;
    }

    // Endpoint to manually trigger sync of offline users to Firebase
    @PostMapping("/sync")
    @Operation(
        summary = "Synchroniser les utilisateurs hors ligne",
        description = "Déclenche manuellement la synchronisation des utilisateurs créés localement mais non encore poussés vers Firebase Auth et Firestore"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronisation terminée avec succès"),
        @ApiResponse(responseCode = "503", description = "Pas de connexion Internet disponible")
    })
    public ResponseEntity<?> syncOfflineUsers() {
        if (!NetworkUtil.hasInternetConnection()) {
            return ResponseEntity.status(503).body("Pas de connexion Internet");
        }

        List<User> offlineUsers = userRepository.findBySyncedFalse();
        int success = 0;
        StringBuilder failed = new StringBuilder();

        for (User u : offlineUsers) {
            if (u.getPassword() == null) {
                failed.append(u.getEmail()).append(" (no password), ");
                continue;
            }
            try {
                String firebaseUid = FirebaseUtils.register(u.getEmail(), u.getPassword());
                u.setFirebaseDocId(firebaseUid);
                u.setSynced(true);
                userRepository.save(u);
                success++;
            } catch (Exception e) {
                failed.append(u.getEmail()).append(" (").append(e.getMessage()).append("), ");
                System.err.println("Sync error for " + u.getEmail() + " : " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Synced: " + success + "; Failed: " + failed.toString());
    }

    // Note: scheduled sync is handled in FirebaseService to avoid duplication

    // ======================
    // LOGIN
    // ======================
    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Authentifie un utilisateur via Firebase (en ligne) ou localement (hors ligne)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connexion réussie"),
        @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé localement")
    })
    public ResponseEntity<?> login(@Parameter(description = "Données de connexion") @RequestBody LoginRequest req) {

        // ===== MODE EN LIGNE (Firebase) =====
        if (NetworkUtil.hasInternetConnection()) {
            try {
                String firebaseUid = FirebaseUtils.login(
                    req.getEmail(),
                    req.getPassword()
                );

                return userRepository.findByFirebaseDocId(firebaseUid)
                        .<ResponseEntity<?>>map(ResponseEntity::ok)
                        .orElse(ResponseEntity.status(404)
                                .body("Utilisateur non synchronisé localement"));

            } catch (Exception e) {
                return ResponseEntity.status(401).body("Login Firebase invalide");
            }
        }

        // ===== MODE HORS LIGNE (LOCAL) =====
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);

        if (user == null ||
            !req.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401)
                    .body("Email ou mot de passe incorrect");
        }

        return ResponseEntity.ok(user);
    }


    @PostMapping("/register")
    @Operation(summary = "Inscription utilisateur", description = "Enregistre un nouvel utilisateur dans Firebase (en ligne) ou localement (hors ligne)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inscription réussie"),
        @ApiResponse(responseCode = "400", description = "Utilisateur déjà existant ou données invalides"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de l'inscription")
    })
    public ResponseEntity<?> register(@Parameter(description = "Données de l'utilisateur à créer") @RequestBody User user) {

        // Vérifie si l'utilisateur existe déjà localement
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Utilisateur déjà existant");
        }

        try {
            // ===== MODE EN LIGNE =====
            if (NetworkUtil.hasInternetConnection()) {
                try {
                    // Enregistrement dans Firebase
                    String firebaseUid = FirebaseUtils.register(user.getEmail(), user.getPassword());

                          // Mise à jour de l'utilisateur pour PostgreSQL
                          user.setFirebaseDocId(firebaseUid);
                          user.setSynced(true);

                } catch (Exception e) {
                    // Si Firebase échoue, on ne bloque pas l'enregistrement local
                    user.setSynced(false);
                    System.err.println("Erreur Firebase : " + e.getMessage());
                }
            } 
            // ===== MODE HORS LIGNE =====
            else {
                if (user.getPassword() == null) {
                    return ResponseEntity.badRequest()
                            .body("Mot de passe requis hors ligne");
                }

                // Encode le mot de passe localement
                user.setPassword(user.getPassword());
                user.setSynced(false);
            }

            // Sauvegarde dans PostgreSQL
            User savedUser = userRepository.save(user);

            // ===== Tentative de push dans Firebase si l'utilisateur a été enregistré hors ligne mais maintenant connecté =====
            if (!savedUser.isSynced() && NetworkUtil.hasInternetConnection() && savedUser.getPassword() != null) {
                try {
                    String firebaseUid = FirebaseUtils.register(savedUser.getEmail(), savedUser.getPassword());
                    savedUser.setFirebaseDocId(firebaseUid);
                    savedUser.setSynced(true);
                    userRepository.save(savedUser); // Mise à jour de PostgreSQL
                } catch (Exception e) {
                    System.err.println("Impossible de synchroniser l'utilisateur hors ligne avec Firebase : " + e.getMessage());
                }
            }

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

     // Get list of blocked users
        @GetMapping("/blocked")
        public ResponseEntity<?> getBlockedUsers() {
                try {
                    List<User> blocked = userRepository.findByStatusBlocage(STATUS_BLOQUE);
                    return ResponseEntity.ok(blocked);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(500).body("Erreur récupération users bloqués: " + e.getMessage());
                }
        }

        // Unblock a list of users by their IDs and sync to Firebase
        @PostMapping("/unblock")
        public ResponseEntity<?> unblockUsers(@RequestBody List<Long> ids) {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body("Liste d'IDs vide");
            }

            List<Long> success = new ArrayList<>();
            Map<Long, String> failed = new java.util.HashMap<>();

            for (Long id : ids) {
                try {
                    userRepository.findById(id).ifPresentOrElse(u -> {
                        u.setIdStatusBlocage(STATUS_ACTIF);
                        userRepository.save(u);
                        success.add(u.getId_user());
                    }, () -> failed.put(id, "Utilisateur introuvable"));
                } catch (Exception e) {
                    failed.put(id, e.getMessage());
                }
            }

            return ResponseEntity.ok("Unblocked: " + success.size() + "; Failed: " + failed.toString());
        }
    


        @GetMapping("/users")
        @Operation(summary = "Lister les utilisateurs Firebase", description = "Récupère tous les utilisateurs depuis Firebase")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs Firebase récupérée"),
            @ApiResponse(responseCode = "500", description = "Erreur Firebase")
        })
        public ResponseEntity<?> getAllFirebaseUsers() {
            try {
                List<ExportedUserRecord> users = firebaseUserService.listAllUsers();
                return ResponseEntity.ok(users);
            } catch (Exception e) {
                return ResponseEntity.status(500)
                        .body("Erreur Firebase : " + e.getMessage());
            }
        }

        @GetMapping("/users/postgres")
        @Operation(summary = "Lister les utilisateurs PostgreSQL", description = "Récupère tous les utilisateurs depuis la base de données locale")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs PostgreSQL récupérée"),
            @ApiResponse(responseCode = "500", description = "Erreur PostgreSQL")
        })
        public ResponseEntity<?> getAllPostgresUsers() {
            try {
                List<User> users = userRepository.findAll();
                return ResponseEntity.ok(users);
            } catch (Exception e) {
                return ResponseEntity.status(500)
                        .body("Erreur PostgreSQL : " + e.getMessage());
            }
        }

        @DeleteMapping("/firebase/users")
        @Operation(summary = "Supprimer tous les utilisateurs Firebase", description = "Supprime tous les utilisateurs de Firebase (opération dangereuse)")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tous les utilisateurs Firebase supprimés"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de la suppression")
        })
    public ResponseEntity<?> deleteAllFirebaseUsers() {
        try {
            firebaseUserService.deleteAllUsers();
            return ResponseEntity.ok("Tous les utilisateurs Firebase ont été supprimés");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Erreur suppression Firebase : " + e.getMessage());
        }
    }
    


}


