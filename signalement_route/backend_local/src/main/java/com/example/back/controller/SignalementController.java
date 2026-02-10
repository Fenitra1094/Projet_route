package com.example.back.controller;

import com.example.back.dto.SignalementSummaryDto;
import com.example.back.dto.SignalementRecapDto;
import com.example.back.models.Signalement;
import com.example.back.repository.SignalementRepository;
import com.example.back.service.SignalementService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Tag(name = "Signalements", description = "API de gestion des signalements")
public class SignalementController {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private SignalementService signalementService;

    @GetMapping("/signalements")
    @Operation(summary = "Lister tous les signalements", description = "Récupère la liste complète des signalements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des signalements récupérée avec succès")
    })
    public ResponseEntity<List<Signalement>> getAllSignalements() {
        List<Signalement> signalements = signalementRepository.findAll();
        return ResponseEntity.ok(signalements);
    }

    @GetMapping("/signalements/summary")
    @Operation(summary = "Résumé des signalements", description = "Récupère un résumé des signalements par arrondissement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résumé des signalements récupéré avec succès")
    })
    public ResponseEntity<List<SignalementSummaryDto>> getSignalementSummary() {
        List<SignalementSummaryDto> summary = signalementService.getSignalementSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/signalements/recapitulatif")
    @Operation(summary = "Récapitulatif des signalements", description = "Récupère un récapitulatif global des signalements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Récapitulatif des signalements récupéré avec succès")
    })
    public ResponseEntity<SignalementRecapDto> getSignalementRecapitulatif() {
        SignalementRecapDto recap = signalementService.getSignalementRecapitulatif();
        return ResponseEntity.ok(recap);
    }
}