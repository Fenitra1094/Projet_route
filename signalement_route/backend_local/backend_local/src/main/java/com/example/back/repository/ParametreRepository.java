package com.example.back.repository;

import com.example.back.models.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Long> {

}
