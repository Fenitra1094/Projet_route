package com.example.cartemigration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRole;

    private String libelle;

    // 🔴 Obligatoire JPA
    protected Role() {
    }

    // ✅ Constructeur métier principal
    public Role(String libelle) {
        this.libelle = libelle;
    }

    // Constructeur complet avec id (pour tests)
    public Role(Long idRole, String libelle) {
        this.idRole = idRole;
        this.libelle = libelle;
    }

    // Getters & Setters
    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
