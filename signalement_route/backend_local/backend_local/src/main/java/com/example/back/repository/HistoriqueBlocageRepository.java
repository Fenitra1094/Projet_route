package com.example.back.repository;

import com.example.back.models.HistoriqueBlocage;
import com.example.back.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoriqueBlocageRepository extends JpaRepository<HistoriqueBlocage, Long> {
    List<HistoriqueBlocage> findByUser(User user);
}
