package com.example.back.controller;
import com.example.back.models.User;
import com.example.back.repository.UserRepository;
import com.example.back.util.NetworkUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.back.util.*;
import com.example.back.dto.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private FirebaseUtils firebaseUtils;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ======================
    // LOGIN
    // ======================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody LoginRequest req
    ) {

        // 🔹 CAS 1 : Firebase (en ligne)
        if (authorization != null && authorization.startsWith("Bearer ")) {

            String token = authorization.replace("Bearer ", "");

            String firebaseUid;
            try {
                firebaseUid = FirebaseUtils.verifyToken(token);
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Token Firebase invalide");
            }

            return userRepository.findByFirebaseUid(firebaseUid)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(404).body("Utilisateur non enregistré localement"));
        }

        // 🔹 CAS 2 : Local
        User user = userRepository.findByEmail(req.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Email ou mot de passe incorrect");
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user
    ) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Utilisateur déjà existant");
        }

        // 🔹 Vérifier la connexion Internet
        if (NetworkUtil.hasInternetConnection()) {
            // Créer dans Firebase
            try {
                String firebaseUid = FirebaseUtils.createUser(user); // méthode à implémenter pour créer utilisateur Firebase
                user.setFirebaseUid(firebaseUid);
                user.setSynced(true);
                user.setPassword(null); // Firebase gère le mot de passe
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Erreur Firebase : " + e.getMessage());
            }
        } else {
            // Création locale uniquement
            if (user.getPassword() == null) {
                return ResponseEntity.badRequest().body("Mot de passe requis en mode local");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setSynced(false);
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }


}


