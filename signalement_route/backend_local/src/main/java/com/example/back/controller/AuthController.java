package com.example.back.controller;

import com.example.back.dto.LoginRequest;
import com.example.back.dto.LoginResponse;
import com.example.back.models.*;
import com.example.back.repository.*;
import com.example.back.util.FirebaseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
            // 1. Vérifier Firebase token
            String firebaseUid = FirebaseUtils.verifyToken(loginRequest.getPassword());
            
            // 2. Chercher l'utilisateur dans la base PostgreSQL
            Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Utilisateur non trouvé. Veuillez vous inscrire.");
            }
            
            User user = userOpt.get();
            
            // 3. Vérifier si l'utilisateur est bloqué
            // Utilisez la méthode corrigée
            Optional<HistoriqueBlocage> dernierBlocageOpt = 
                historiqueBlocageRepository.findLatestByUserId(user.getId_user());
            
            if (dernierBlocageOpt.isPresent()) {
                HistoriqueBlocage dernierBlocage = dernierBlocageOpt.get();
                StatusBlocage status = dernierBlocage.getStatusBlocage();
                
                // Vérifier si le dernier statut est "Bloqué"
                if ("Bloqué".equalsIgnoreCase(status.getStatus())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Votre compte est bloqué. Date du blocage: " + 
                                  dernierBlocage.getDate());
                }
            }
            
            // 4. Réponse avec les données utilisateur
            LoginResponse response = new LoginResponse(
                user.getId_user(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole().getLibelle(),
                firebaseUid
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Échec de l'authentification: " + e.getMessage());
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