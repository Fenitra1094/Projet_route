package com.example.back.repository;

import com.example.back.models.HistoriqueStatus;
import com.example.back.models.Signalement;
import com.example.back.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueStatusRepository extends JpaRepository<HistoriqueStatus, Long> {

    // Trouver l'historique pour un signalement
    List<HistoriqueStatus> findBySignalementOrderByDateChangementAsc(Signalement signalement);

    // Trouver l'historique pour un signalement et un statut
    List<HistoriqueStatus> findBySignalementAndStatus(Signalement signalement, Status status);

    // Trouver la première occurrence d'un statut pour un signalement
    @Query("SELECT h FROM HistoriqueStatus h WHERE h.signalement = :signalement AND h.status.libelle = :statusLibelle ORDER BY h.dateChangement ASC")
    List<HistoriqueStatus> findFirstBySignalementAndStatusLibelle(@Param("signalement") Signalement signalement, @Param("statusLibelle") String statusLibelle);

    // Trouver tous les changements vers un statut final
    @Query("SELECT h FROM HistoriqueStatus h WHERE h.status.libelle = :statusLibelle ORDER BY h.dateChangement DESC")
    List<HistoriqueStatus> findByStatusLibelle(@Param("statusLibelle") String statusLibelle);

}