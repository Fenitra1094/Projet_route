package com.example.cartemigration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entreprise")
public class Entreprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEntreprise;

    private String entreprise;

    // 🔴 Obligatoire JPA
    protected Entreprise() {
    }

    // ✅ Constructeur métier principal
    public Entreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    // Constructeur complet avec id (pour tests)
    public Entreprise(Long idEntreprise, String entreprise) {
        this.idEntreprise = idEntreprise;
        this.entreprise = entreprise;
    }

    // Getters & Setters
    public Long getIdEntreprise() {
        return idEntreprise;
    }

    public void setIdEntreprise(Long idEntreprise) {
        this.idEntreprise = idEntreprise;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }
}
