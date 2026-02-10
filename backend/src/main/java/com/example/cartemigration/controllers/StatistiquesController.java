package com.example.cartemigration.controllers;

import com.example.cartemigration.dto.StatistiquesDTO;
import com.example.cartemigration.services.SignalementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/stats")
public class StatistiquesController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatistiquesController.class);
    
    private final SignalementService signalementService;
    
    public StatistiquesController(SignalementService signalementService) {
        this.signalementService = signalementService;
    }
    
    /**
     * GET /api/stats - Récupérer les statistiques globales
     */
    @GetMapping
    public ResponseEntity<StatistiquesDTO> getStatistiques() {
        try {
            StatistiquesDTO stats = signalementService.getStatistiques();
            logger.info("📊 Stats retrieval successful: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des statistiques", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
