package com.example.back.repository;

import com.example.back.models.StatusBlocage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StatusBlocageRepository extends JpaRepository<StatusBlocage, Long> {
    Optional<StatusBlocage> findByStatus(String status);
}
