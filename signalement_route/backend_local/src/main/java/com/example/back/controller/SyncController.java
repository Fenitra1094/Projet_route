// SyncController.java
package com.example.back.controller;

import com.example.back.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin
public class SyncController {
    
    @Autowired
    private FirebaseService firebaseService;
    
    @PostMapping("/to-firebase")
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
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        try {
            Map<String, Object> status = firebaseService.getSyncStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/signalements/to-firebase")
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