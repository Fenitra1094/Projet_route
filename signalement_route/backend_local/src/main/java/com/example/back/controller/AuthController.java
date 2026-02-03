package com.example.back.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.back.dto.LoginRequest;
import com.example.back.dto.LoginResponse;
import com.example.back.models.HistoriqueBlocage;
import com.example.back.models.User;
import com.example.back.repository.HistoriqueBlocageRepository;
import com.example.back.repository.StatusBlocageRepository;
import com.example.back.repository.UserRepository;
import com.example.back.util.FirebaseUtils;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final HistoriqueBlocageRepository historiqueBlocageRepository;
    private final StatusBlocageRepository statusBlocageRepository;

    public AuthController(UserRepository userRepository,
                         HistoriqueBlocageRepository historiqueBlocageRepository,
                         StatusBlocageRepository statusBlocageRepository) {
        this.userRepository = userRepository;
        this.historiqueBlocageRepository = historiqueBlocageRepository;
        this.statusBlocageRepository = statusBlocageRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Vérifier Firebase token uniquement
            String firebaseUid = FirebaseUtils.verifyToken(loginRequest.getPassword());
            
            // 2. Si le token Firebase est valide, retourner une réponse de succès
            // L'utilisateur est authentifié via Firebase
            LoginResponse response = new LoginResponse(
                null,  // ID utilisateur non disponible sans la base de données
                null,  // Email non disponible
                null,  // Nom non disponible
                null,  // Prénom non disponible
                null,  // Rôle non disponible
                firebaseUid
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Échec de l'authentification Firebase: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserByFirebaseUid(@RequestParam String uid) {
        try {
            Optional<User> userOpt = userRepository.findByFirebaseUid(uid);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Utilisateur non trouvé");
            }
            
            User user = userOpt.get();
            
            // Vérifier si bloqué en utilisant la méthode corrigée
            Optional<HistoriqueBlocage> blocageOpt = 
                historiqueBlocageRepository.findLatestByUserId(user.getId_user());
            
            if (blocageOpt.isPresent() && 
                "Bloqué".equalsIgnoreCase(blocageOpt.get().getStatusBlocage().getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Compte bloqué");
            }
            
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur: " + e.getMessage());
        }
    }
}