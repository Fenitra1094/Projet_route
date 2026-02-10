package com.example.back.controller;
import com.example.back.models.User;
import com.example.back.repository.UserRepository;
import com.example.back.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import com.google.firebase.auth.ExportedUserRecord;
import com.example.back.dto.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // status IDs inserted by migration: 1 = actif, 2 = bloque
    private static final Integer STATUS_ACTIF = 1;
    private static final Integer STATUS_BLOQUE = 2;

    
    public AuthController(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ======================
    // LOGIN
    // ======================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            System.out.println("========== LOGIN ATTEMPT ==========");
            System.out.println("Email reçu: " + req.getEmail());
            System.out.println("Password reçu: " + (req.getPassword() != null ? "***" : "null"));
            
            User user = userRepository.findByEmail(req.getEmail()).orElse(null);

            // If user not found -> generic auth error
            if (user == null) {
                System.err.println("ERREUR: Utilisateur non trouvé pour l'email: " + req.getEmail());
                return ResponseEntity.status(401)
                        .body("Email ou mot de passe incorrect");
            }

            System.out.println("Utilisateur trouvé: " + user.getEmail());
            System.out.println("Role de l'utilisateur: " + user.getId_role());
            System.out.println("Statut blocage: " + user.getIdStatusBlocage());
            System.out.println("Password hashé en DB: " + (user.getPassword() != null ? "***" : "null"));

            // If role is not 2, hide existence with a generic message
            if (user.getId_role() == null || !user.getId_role().equals(2)) {
                System.err.println("ERREUR: Role incorrect. Requis: 2, Actuel: " + user.getId_role());
                return ResponseEntity.status(401)
                        .body("identifiant non connu");
            }

            // Verify password
            if (user.getPassword() == null) {
                System.err.println("ERREUR: Pas de mot de passe en base pour cet utilisateur");
                return ResponseEntity.status(401)
                        .body("Email ou mot de passe incorrect");
            }

            // Comparaison simple sans BCrypt
            boolean passwordMatch = user.getPassword().equals(req.getPassword());
            System.out.println("Correspondance mot de passe: " + passwordMatch);
            
            if (!passwordMatch) {
                System.err.println("ERREUR: Mot de passe incorrect");
                return ResponseEntity.status(401)
                        .body("Email ou mot de passe incorrect");
            }

            System.out.println("LOGIN RÉUSSI pour: " + user.getEmail());
            System.out.println("===================================");
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            System.err.println("EXCEPTION lors du login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Erreur serveur: " + e.getMessage());
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        // Vérifie si l'utilisateur existe déjà localement
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Utilisateur déjà existant");
        }

        try {
            // Ensure default blocked status -> set status to 'actif'
            user.setIdStatusBlocage(STATUS_ACTIF);

            
                // Offline: save locally, mark as not synced
                if (user.getPassword() == null) {
                    return ResponseEntity.badRequest()
                            .body("Mot de passe requis hors ligne");
                }

                User savedUser = userRepository.save(user);
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
    


}


