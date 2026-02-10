package com.example.back.controller;

import com.example.back.models.User;
import com.example.back.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
@Tag(name = "Utilisateurs", description = "API de gestion des utilisateurs")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @Operation(
        summary = "Lister tous les utilisateurs",
        description = "Récupère la liste complète des utilisateurs enregistrés dans la base de données, " +
                     "incluant leurs informations de profil et rôles."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtenir un utilisateur par ID",
        description = "Récupère les détails complets d'un utilisateur spécifique par son ID PostgreSQL"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur trouvé",
                    content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID de l'utilisateur à récupérer", example = "1")
            @PathVariable Long id) {
        try {
            return userRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un utilisateur", description = "Modifie les informations d'un utilisateur existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID de l'utilisateur à mettre à jour") @PathVariable Long id,
            @Parameter(description = "Nouvelles données de l'utilisateur") @RequestBody User userDetails) {
        try {
            return userRepository.findById(id)
                    .map(user -> {
                        user.setEmail(userDetails.getEmail());
                        user.setNom(userDetails.getNom());
                        user.setPrenom(userDetails.getPrenom());
                        user.setId_role(userDetails.getId_role());
                        user.setFirebaseDocId(userDetails.getFirebaseDocId());
                        user.setSynced(userDetails.isSynced());
                        User updatedUser = userRepository.save(user);
                        return ResponseEntity.ok(updatedUser);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour partiellement un utilisateur", description = "Modifie partiellement les informations d'un utilisateur existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<User> patchUser(
            @Parameter(description = "ID de l'utilisateur à mettre à jour") @PathVariable Long id,
            @Parameter(description = "Données partielles de l'utilisateur") @RequestBody User userDetails) {
        try {
            return userRepository.findById(id)
                    .map(user -> {
                        if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
                        if (userDetails.getNom() != null) user.setNom(userDetails.getNom());
                        if (userDetails.getPrenom() != null) user.setPrenom(userDetails.getPrenom());
                        if (userDetails.getId_role() != null) user.setId_role(userDetails.getId_role());
                        if (userDetails.getFirebaseDocId() != null) user.setFirebaseDocId(userDetails.getFirebaseDocId());
                        user.setSynced(userDetails.isSynced()); // boolean, always set
                        User updatedUser = userRepository.save(user);
                        return ResponseEntity.ok(updatedUser);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}