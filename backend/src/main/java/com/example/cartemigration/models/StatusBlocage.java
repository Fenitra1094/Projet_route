package com.example.cartemigration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "status_blocage")
public class StatusBlocage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStatusBlocage;

    private String status;

    // 🔴 Obligatoire JPA
    protected StatusBlocage() {
    }

    // ✅ Constructeur métier principal
    public StatusBlocage(String status) {
        this.status = status;
    }

    // Constructeur complet avec id (pour tests)
    public StatusBlocage(Long idStatusBlocage, String status) {
        this.idStatusBlocage = idStatusBlocage;
        this.status = status;
    }

    // Getters & Setters
    public Long getIdStatusBlocage() {
        return idStatusBlocage;
    }

    public void setIdStatusBlocage(Long idStatusBlocage) {
        this.idStatusBlocage = idStatusBlocage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
