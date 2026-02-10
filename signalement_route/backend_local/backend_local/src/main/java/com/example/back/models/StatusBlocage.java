package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "status_blocage")
public class StatusBlocage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status_blocage")
    private Long idStatusBlocage;

    @Column(name = "status", length = 50)
    private String status;

    // Constructeur vide obligatoire pour JPA
    public StatusBlocage() {}

    // Constructeur pratique
    public StatusBlocage(String status) {
        this.status = status;
    }

    // Getters et setters
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

    @Override
    public String toString() {
        return "StatusBlocage{" +
                "idStatusBlocage=" + idStatusBlocage +
                ", status='" + status + '\'' +
                '}';
    }
}
