package com.example.back.repository;

import com.example.back.models.Quartier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuartierRepository extends JpaRepository<Quartier, Long> {
    @Query("SELECT q FROM Quartier q WHERE q.province.id_province = :provinceId")
    List<Quartier> findByProvinceId(@Param("provinceId") Long provinceId);
}