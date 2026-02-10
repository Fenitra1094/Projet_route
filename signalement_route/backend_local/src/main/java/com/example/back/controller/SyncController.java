// SyncController.java
package com.example.back.controller;

import com.example.back.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin
@Tag(name = "Synchronisation", description = "API de synchronisation entre PostgreSQL et Firebase")
public class SyncController {
    
    @Autowired
    private FirebaseService firebaseService;
    
    @PostMapping("/to-firebase")
    @Operation(summary = "Synchroniser vers Firebase", description = "Synchronise toutes les données locales vers Firebase")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronisation vers Firebase réussie"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la synchronisation")
    })
    public ResponseEntity<String> syncToFirebase() {
        try {
            firebaseService.syncAllToFirebase();
            return ResponseEntity.ok("✅ Synchronisation vers Firebase terminée avec succès");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur lors de la synchronisation vers Firebase: " + e.getMessage());
        }
    }
    
    @PostMapping("/from-firebase")
    @Operation(summary = "Synchroniser depuis Firebase", description = "Synchronise toutes les données Firebase vers la base locale")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronisation depuis Firebase réussie"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la synchronisation")
    })
    public ResponseEntity<String> syncFromFirebase() {
        try {
            firebaseService.syncAllFromFirebase();
            return ResponseEntity.ok("✅ Synchronisation depuis Firebase terminée avec succès");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur lors de la synchronisation depuis Firebase: " + e.getMessage());
        }
    }
    
    @PostMapping("/full")
    @Operation(summary = "Synchronisation complète", description = "Effectue une synchronisation bidirectionnelle complète")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Synchronisation complète réussie"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la synchronisation complète")
    })
    public ResponseEntity<String> fullSync() {
        try {
            firebaseService.syncAllToFirebase();
            firebaseService.syncAllFromFirebase();
            return ResponseEntity.ok("✅ Synchronisation complète terminée avec succès");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur lors de la synchronisation complète: " + e.getMessage());
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Statut de synchronisation", description = "Récupère le statut actuel de la synchronisation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut de synchronisation récupéré"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la récupération du statut")
    })
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        try {
            Map<String, Object> status = firebaseService.getSyncStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/signalements/to-firebase")
    @Operation(summary = "Synchroniser les signalements vers Firebase", description = "Synchronise uniquement les signalements vers Firebase")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Signalements synchronisés avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur lors de la synchronisation des signalements")
    })
    public ResponseEntity<String> syncSignalementsToFirebase() {
        try {
            firebaseService.syncSignalementsToFirebase();
            return ResponseEntity.ok("✅ Signalements synchronisés vers Firebase avec succès");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Erreur synchronisation signalements: " + e.getMessage());
        }
    }
}