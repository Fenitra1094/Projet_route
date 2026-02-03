package com.example.back.repository;

import com.example.back.models.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    Signalement findByFirebaseDocId(String firebaseDocId);
    List<Signalement> findByStatusIdStatus(Long statusId);
    @Query("SELECT s FROM Signalement s WHERE s.date = :date AND s.user.id_user = :userId")
    List<Signalement> findByDateAndUserId(@Param("date") LocalDateTime date, 
                                         @Param("userId") Long userId);
}