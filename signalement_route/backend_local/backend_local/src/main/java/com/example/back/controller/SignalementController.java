package com.example.back.controller;

import com.example.back.dto.SignalementSummaryDto;
import com.example.back.dto.SignalementRecapDto;
import com.example.back.models.Signalement;
import com.example.back.repository.SignalementRepository;
import com.example.back.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/signalements")
    @Operation(
        summary = "Lister tous les signalements",
        description = "Récupère la liste complète des signalements avec toutes leurs informations détaillées"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des signalements récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Signalement.class)))
    })
    public ResponseEntity<List<Signalement>> getAllSignalements() {
        List<Signalement> signalements = signalementRepository.findAll();
        return ResponseEntity.ok(signalements);
    }

    @GetMapping("/signalements/summary")
    @Operation(
        summary = "Résumé des signalements par arrondissement",
        description = "Récupère un résumé statistique des signalements groupés par arrondissement"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résumé des signalements récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = SignalementSummaryDto.class)))
    })
    public ResponseEntity<List<SignalementSummaryDto>> getSignalementSummary() {
        List<SignalementSummaryDto> summary = signalementService.getSignalementSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/signalements/recapitulatif")
    @Operation(
        summary = "Récapitulatif global des signalements",
        description = "Récupère un récapitulatif complet avec statistiques générales : nombre total, par statut, par arrondissement, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Récapitulatif des signalements récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = SignalementRecapDto.class)))
    })
    public ResponseEntity<SignalementRecapDto> getSignalementRecapitulatif() {
        SignalementRecapDto recap = signalementService.getSignalementRecapitulatif();
        return ResponseEntity.ok(recap);
    }

    @GetMapping("/signalements/delai-traitement-moyen")
    @Operation(
        summary = "Délai de traitement moyen",
        description = "Calcule le délai moyen de traitement des travaux terminés en jours. " +
                     "Le calcul prend la date de création de chaque signalement et la date de passage au statut final spécifié, " +
                     "puis fait la moyenne des durées. Exemple: si un signalement est créé le 2026-02-04 et terminé le 2026-02-05, " +
                     "la durée est de 1 jour."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Délai moyen calculé avec succès", content = @Content(schema = @Schema(type = "number", format = "double", example = "1.0"))),
        @ApiResponse(responseCode = "500", description = "Erreur lors du calcul")
    })
    public ResponseEntity<Double> getDelaiTraitementMoyen(
            @Parameter(description = "Statut final pour le calcul (ex: 'termine', 'en cours')", example = "termine")
            @RequestParam(defaultValue = "termine") String statusFinal) {
        double averageDays = firebaseService.getAverageProcessingTime(statusFinal);
        return ResponseEntity.ok(averageDays);
    }
}