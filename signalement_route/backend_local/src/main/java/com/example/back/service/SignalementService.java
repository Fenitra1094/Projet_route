package com.example.back.service;

import com.example.back.dto.SignalementSummaryDto;
import com.example.back.dto.SignalementRecapDto;
import com.example.back.dto.SignalementParArrondissementDto;
import com.example.back.models.Signalement;
import com.example.back.repository.SignalementRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepository;

    public SignalementService(SignalementRepository signalementRepository) {
        this.signalementRepository = signalementRepository;
    }

    public List<SignalementSummaryDto> getSignalementSummary() {
        List<Signalement> signalements = signalementRepository.findAll();
        return signalements.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private SignalementSummaryDto convertToDto(Signalement signalement) {
        return new SignalementSummaryDto(
            signalement.getIdSignalement(),
            signalement.getDate() != null ? signalement.getDate().toString() : "",
            signalement.getSurface() != null ? signalement.getSurface().toString() : "",
            signalement.getBudget() != null ? signalement.getBudget().toString() : "",
            signalement.getUser() != null ? signalement.getUser().getNom() + " " + signalement.getUser().getPrenom() : "",
            signalement.getEntreprise() != null ? signalement.getEntreprise().getEntreprise() : "",
            signalement.getQuartier() != null ? signalement.getQuartier().getQuartier() : "",
            signalement.getStatus() != null ? signalement.getStatus().getLibelle() : ""
        );
    }

    public SignalementRecapDto getSignalementRecapitulatif() {
        List<Signalement> signalements = signalementRepository.findAll();
        
        // Nombre de points signalés
        Long nombrePoints = (long) signalements.size();
        
        // Surface totale traitée
        BigDecimal surfaceTotal = signalements.stream()
            .filter(s -> s.getSurface() != null)
            .map(Signalement::getSurface)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Budget total alloué
        BigDecimal budgetTotal = signalements.stream()
            .filter(s -> s.getBudget() != null)
            .map(Signalement::getBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Taux d'avancement (nombre de signalements avec status "complété" ou "en cours" / total)
        long signalementsClosed = signalements.stream()
            .filter(s -> s.getStatus() != null && 
                    (s.getStatus().getLibelle().equalsIgnoreCase("Complété") ||
                     s.getStatus().getLibelle().equalsIgnoreCase("En cours")))
            .count();
        Double tauxAvancement = nombrePoints > 0 ? (double) signalementsClosed / nombrePoints * 100 : 0.0;
        
        // Grouper par arrondissement (quartier)
        Map<String, List<Signalement>> signalementsByArrondissement = signalements.stream()
            .filter(s -> s.getQuartier() != null)
            .collect(Collectors.groupingBy(s -> s.getQuartier().getQuartier()));
        
        // Créer les détails par arrondissement
        List<SignalementParArrondissementDto> detailsParArrondissement = signalementsByArrondissement
            .entrySet().stream()
            .map(entry -> {
                String arrondissement = entry.getKey();
                List<Signalement> signalementsList = entry.getValue();
                
                Long points = (long) signalementsList.size();
                BigDecimal surface = signalementsList.stream()
                    .filter(s -> s.getSurface() != null)
                    .map(Signalement::getSurface)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal budget = signalementsList.stream()
                    .filter(s -> s.getBudget() != null)
                    .map(Signalement::getBudget)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                long pointsCompletes = signalementsList.stream()
                    .filter(s -> s.getStatus() != null && 
                            (s.getStatus().getLibelle().equalsIgnoreCase("Complété") ||
                             s.getStatus().getLibelle().equalsIgnoreCase("En cours")))
                    .count();
                Double avancement = points > 0 ? (double) pointsCompletes / points * 100 : 0.0;
                
                return new SignalementParArrondissementDto(arrondissement, points, surface, avancement, budget);
            })
            .collect(Collectors.toList());
        
        return new SignalementRecapDto(nombrePoints, surfaceTotal, tauxAvancement, budgetTotal, detailsParArrondissement);
    }
}