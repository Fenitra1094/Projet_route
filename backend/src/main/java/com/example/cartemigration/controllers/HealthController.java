package com.example.cartemigration.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    /**
     * GET /api/health - Vérifier l'état du serveur
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "✅ OK");
        response.put("application", "Carte Migration API");
        response.put("version", "1.0.0");
        response.put("database", "PostgreSQL connected");
        
        return ResponseEntity.ok(response);
    }
}
