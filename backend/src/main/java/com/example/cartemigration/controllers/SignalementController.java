package com.example.cartemigration.controllers;

import com.example.cartemigration.models.Signalement;
import com.example.cartemigration.dto.StatistiquesDTO;
import com.example.cartemigration.services.SignalementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/signalements")
public class SignalementController {
    
    private final SignalementService signalementService;
    
    public SignalementController(SignalementService signalementService) {
        this.signalementService = signalementService;
    }
    
    /**
     * GET /api/signalements - Récupérer tous les signalements
     */
    @GetMapping
    public ResponseEntity<List<Signalement>> getAllSignalements() {
        List<Signalement> signalements = signalementService.getAllSignalements();
        return ResponseEntity.ok(signalements);
    }
    
    /**
     * GET /api/signalements/{id} - Récupérer un signalement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Signalement> getSignalementById(@PathVariable Long id) {
        return signalementService.getSignalementById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * POST /api/signalements - Créer un nouveau signalement
     */
    @PostMapping
    public ResponseEntity<Signalement> createSignalement(@RequestBody Signalement signalement) {
        Signalement created = signalementService.createSignalement(signalement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/signalements/{id} - Mettre à jour un signalement
     */
    @PutMapping("/{id}")
    public ResponseEntity<Signalement> updateSignalement(
            @PathVariable Long id,
            @RequestBody Signalement signalement) {
        try {
            Signalement updated = signalementService.updateSignalement(id, signalement);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * DELETE /api/signalements/{id} - Supprimer un signalement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSignalement(@PathVariable Long id) {
        if (signalementService.getSignalementById(id).isPresent()) {
            signalementService.deleteSignalement(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

