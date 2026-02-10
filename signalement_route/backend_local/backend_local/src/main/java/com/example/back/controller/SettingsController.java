package com.example.back.controller;

import com.example.back.models.Parametre;
import com.example.back.repository.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/settings")
@CrossOrigin
public class SettingsController {

    private final ParametreRepository parametreRepository;

    @Autowired
    public SettingsController(ParametreRepository parametreRepository) {
        this.parametreRepository = parametreRepository;
    }

    // Save or update settings - stores in local `parametre` table
    @PostMapping
    public ResponseEntity<?> saveSettings(@RequestBody Parametre p) {
        try {
            // Basic validation
            if (p == null) {
                return ResponseEntity.badRequest().body("Corps de requête manquant");
            }
            if (p.getDureeSession() == null || p.getDureeSession() <= 0) {
                return ResponseEntity.badRequest().body("dureeSession doit être un entier positif");
            }
            if (p.getNombreTentative() == null || p.getNombreTentative() <= 0) {
                return ResponseEntity.badRequest().body("nombreTentative doit être un entier positif");
            }

            // Simple behaviour: if an id is provided, update; otherwise create new
            Parametre saved = parametreRepository.save(p);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur sauvegarde paramètre: " + e.getMessage());
        }
    }

    // Return all settings entries (usually single row)
    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<Parametre> all = parametreRepository.findAll();
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lecture paramètres: " + e.getMessage());
        }
    }
}
