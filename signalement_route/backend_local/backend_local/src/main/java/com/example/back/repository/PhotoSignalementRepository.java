package com.example.back.repository;

import com.example.back.models.PhotoSignalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhotoSignalementRepository extends JpaRepository<PhotoSignalement, Long> {
    List<PhotoSignalement> findBySignalement_IdSignalement(Long idSignalement);
}
