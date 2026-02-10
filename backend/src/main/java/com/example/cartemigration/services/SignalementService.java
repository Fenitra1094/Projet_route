package com.example.cartemigration.services;

import com.example.cartemigration.models.Signalement;
import com.example.cartemigration.repositories.SignalementRepository;
import com.example.cartemigration.dto.StatistiquesDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SignalementService {

    private final SignalementRepository signalementRepository;

    public SignalementService(SignalementRepository signalementRepository) {
        this.signalementRepository = signalementRepository;
    }

    public List<Signalement> getAllSignalements() {
        return signalementRepository.findAll();
    }

    public Optional<Signalement> getSignalementById(Long id) {
        return signalementRepository.findById(id);
    }

    public Signalement createSignalement(Signalement signalement) {
        return signalementRepository.save(signalement);
    }

    public Signalement updateSignalement(Long id, Signalement signalementDetails) {
        return signalementRepository.findById(id).map(signalement -> {

            if (signalementDetails.getSurface() != null) {
                signalement.setSurface(signalementDetails.getSurface());
            }

            if (signalementDetails.getBudget() != null) {
                signalement.setBudget(signalementDetails.getBudget());
            }

            if (signalementDetails.getEntreprise() != null) {
                signalement.setEntreprise(signalementDetails.getEntreprise());
            }

            if (signalementDetails.getStatus() != null) {
                signalement.setStatus(signalementDetails.getStatus());
            }

            return signalementRepository.save(signalement);

        }).orElseThrow(() ->
                new RuntimeException("Signalement non trouvé avec ID: " + id)
        );
    }

    public void deleteSignalement(Long id) {
        signalementRepository.deleteById(id);
    }

    public long countByStatus(Long statusId) {
        return signalementRepository.findAll().stream()
                .filter(s -> s.getStatus() != null)
                .filter(s -> s.getStatus().getIdStatus().equals(statusId))
                .count();
    }

    public Double getTotalSurface() {
        return signalementRepository.findAll().stream()
                .map(Signalement::getSurface)
                .filter(java.util.Objects::nonNull)
                .reduce(0.0, Double::sum);
    }

    public Double getTotalBudget() {
        return signalementRepository.findAll().stream()
                .map(Signalement::getBudget)
                .filter(java.util.Objects::nonNull)
                .reduce(0.0, Double::sum);
    }

    // ==================== Nouvelles méthodes pour les stats ====================
    
    public StatistiquesDTO getStatistiques() {
        List<Signalement> signalements = getAllSignalements();
        int totalProblemes = signalements.size();
        
        Double surfaceTotal = getTotalSurface();
        Double budgetTotal = getTotalBudget();
        
        // Calculer le pourcentage de terminés/bloqués
        long termines = signalements.stream()
                .filter(s -> s.getStatus() != null)
                .filter(s -> s.getStatus().getLibelle() != null)
                .filter(s -> s.getStatus().getLibelle().toLowerCase().contains("bloqu") ||
                           s.getStatus().getLibelle().toLowerCase().contains("termin"))
                .count();
        
        Double pourcentageTermines = totalProblemes > 0 
                ? (double) (termines * 100) / totalProblemes 
                : 0.0;
        
        return new StatistiquesDTO(
                totalProblemes,
                surfaceTotal != null ? surfaceTotal : 0.0,
                budgetTotal != null ? budgetTotal : 0.0,
                Math.round(pourcentageTermines * 100.0) / 100.0
        );
    }
}



