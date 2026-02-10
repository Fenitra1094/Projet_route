package com.example.cartemigration.repositories;

import com.example.cartemigration.models.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {
    // À implémenter si besoin de méthodes custom
}
