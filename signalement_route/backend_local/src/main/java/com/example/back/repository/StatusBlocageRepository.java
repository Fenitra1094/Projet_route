package com.example.back.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.back.models.StatusBlocage;

@Repository
public interface StatusBlocageRepository extends JpaRepository<StatusBlocage, Long> {
    
    Optional<StatusBlocage> findByStatus(String status);
}