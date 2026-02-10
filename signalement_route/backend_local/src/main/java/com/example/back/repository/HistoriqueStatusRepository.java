package com.example.back.repository;

import com.example.back.models.HistoriqueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoriqueStatusRepository extends JpaRepository<HistoriqueStatus, Long> {
}
