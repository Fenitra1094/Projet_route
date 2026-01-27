package com.example.back.repository;

import com.example.back.models.HistoriqueBlocage;
import com.example.back.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoriqueBlocageRepository extends JpaRepository<HistoriqueBlocage, Long> {
    
    // Find by User object
    Optional<HistoriqueBlocage> findTopByUserOrderByDateDesc(User user);
    
    // Find latest record by User ID
    @Query("SELECT h FROM HistoriqueBlocage h WHERE h.user.id_user = :userId ORDER BY h.date DESC LIMIT 1")
    Optional<HistoriqueBlocage> findLatestByUserId(@Param("userId") Long userId);
    
    // Find by User's ID using explicit query to handle underscore in field name
    @Query("SELECT h FROM HistoriqueBlocage h WHERE h.user.id_user = :userId")
    Optional<HistoriqueBlocage> findByUserId(@Param("userId") Long userId);
}