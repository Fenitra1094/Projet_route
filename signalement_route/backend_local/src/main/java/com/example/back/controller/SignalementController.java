package com.example.back.controller;

import com.example.back.models.*;
import com.example.back.repository.*;
import com.example.back.dto.SignalementRequest;
import com.example.back.dto.SignalementResponse;
import com.example.back.util.FirebaseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/signalements")
@CrossOrigin(origins = "*")
public class SignalementController {

    private final SignalementRepository signalementRepository;
    private final UserRepository userRepository;
    private final QuartierRepository quartierRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final StatusRepository statusRepository;

    public SignalementController(SignalementRepository signalementRepository,
                                UserRepository userRepository,
                                QuartierRepository quartierRepository,
                                EntrepriseRepository entrepriseRepository,
                                StatusRepository statusRepository) {
        this.signalementRepository = signalementRepository;
        this.userRepository = userRepository;
        this.quartierRepository = quartierRepository;
        this.entrepriseRepository = entrepriseRepository;
        this.statusRepository = statusRepository;
    }

    /**
     * Récupérer tous les signalements
     */
    @GetMapping
    public ResponseEntity<?> getAllSignalements(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extraire le token et vérifier l'utilisateur
            String token = extractToken(authHeader);
            String firebaseUid = FirebaseUtils.verifyToken(token);
            
            // Vérifier que l'utilisateur existe
            User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Récupérer tous les signalements
            List<Signalement> signalements = signalementRepository.findAll();
            
            // Convertir en DTO pour éviter les problèmes de relations circulaires
            List<SignalementResponse> responses = signalements.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération: " + e.getMessage());
        }
    }

    /**
     * Créer un nouveau signalement
     */
    @PostMapping
    public ResponseEntity<?> createSignalement(
            @RequestBody SignalementRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Vérifier l'authentification
            String token = extractToken(authHeader);
            String firebaseUid = FirebaseUtils.verifyToken(token);
            
            // Récupérer l'utilisateur
            User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Récupérer le quartier
            Quartier quartier = quartierRepository.findById(request.getIdQuartier())
                .orElseThrow(() -> new RuntimeException("Quartier non trouvé"));
            
            // Récupérer l'entreprise (si fournie)
            Entreprise entreprise = null;
            if (request.getIdEntreprise() != null) {
                entreprise = entrepriseRepository.findById(request.getIdEntreprise())
                    .orElse(null); // Optionnel, donc pas d'erreur si non trouvé
            }
            
            // Récupérer le statut par défaut ("En attente")
            Status status = statusRepository.findByLibelle("En attente")
                .orElseThrow(() -> new RuntimeException("Statut par défaut non trouvé"));
            
            // Créer le signalement
            Signalement signalement = new Signalement();
            signalement.setDate(LocalDateTime.now());
            signalement.setSurface(request.getSurface());
            signalement.setBudget(request.getBudget());
            signalement.setUser(user);
            signalement.setQuartier(quartier);
            signalement.setEntreprise(entreprise);
            signalement.setStatus(status);
            
            // Sauvegarder
            Signalement savedSignalement = signalementRepository.save(signalement);
            
            // Retourner la réponse
            SignalementResponse response = convertToResponse(savedSignalement);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur lors de la création: " + e.getMessage());
        }
    }

    /**
     * Récupérer un signalement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSignalementById(@PathVariable Long id) {
        try {
            Signalement signalement = signalementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé"));
            
            return ResponseEntity.ok(convertToResponse(signalement));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Récupérer les quartiers pour le formulaire
     */
    @GetMapping("/quartiers")
    public ResponseEntity<?> getAllQuartiers() {
        try {
            List<Quartier> quartiers = quartierRepository.findAll();
            return ResponseEntity.ok(quartiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des quartiers");
        }
    }

    /**
     * Récupérer les entreprises pour le formulaire
     */
    @GetMapping("/entreprises")
    public ResponseEntity<?> getAllEntreprises() {
        try {
            List<Entreprise> entreprises = entrepriseRepository.findAll();
            return ResponseEntity.ok(entreprises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des entreprises");
        }
    }

    /**
     * Convertir Signalement en SignalementResponse (DTO)
     */
    private SignalementResponse convertToResponse(Signalement signalement) {
        SignalementResponse response = new SignalementResponse();
        response.setIdSignalement(signalement.getIdSignalement());
        response.setDate(signalement.getDate());
        response.setSurface(signalement.getSurface());
        response.setBudget(signalement.getBudget());
        response.setUser(signalement.getUser());
        response.setQuartier(signalement.getQuartier());
        response.setEntreprise(signalement.getEntreprise());
        response.setStatus(signalement.getStatus());
        return response;
    }

    /**
     * Extraire le token du header Authorization
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token manquant ou invalide");
    }
}