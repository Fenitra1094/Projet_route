package com.example.back.repository;

import com.example.back.models.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    // Find by Quartier ID using explicit query to handle underscore in field name
    @Query("SELECT s FROM Signalement s WHERE s.quartier.id_quartier = :quartierId")
    List<Signalement> findByQuartierId(@Param("quartierId") Long quartierId);
    
    // Find by User ID using explicit query to handle underscore in field name
    @Query("SELECT s FROM Signalement s WHERE s.user.id_user = :userId")
    List<Signalement> findByUserId(@Param("userId") Long userId);
    
    // Find by Status ID
    @Query("SELECT s FROM Signalement s WHERE s.status.idStatus = :idStatus")
    List<Signalement> findByStatusId(@Param("idStatus") Long idStatus);
}