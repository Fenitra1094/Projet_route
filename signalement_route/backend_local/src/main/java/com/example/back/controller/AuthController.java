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
import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

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
                u.setFirebaseUid(firebaseUid);
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
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        // ===== MODE EN LIGNE (Firebase) =====
        if (NetworkUtil.hasInternetConnection()) {
            try {
                String firebaseUid = FirebaseUtils.login(
                    req.getEmail(),
                    req.getPassword()
                );

                return userRepository.findByFirebaseUid(firebaseUid)
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
    public ResponseEntity<?> register(@RequestBody User user) {

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
                          user.setFirebaseUid(firebaseUid);
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
                    savedUser.setFirebaseUid(firebaseUid);
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

    

        @GetMapping("/users")
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


