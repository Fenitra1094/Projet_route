package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "entreprise")
public class Entreprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entreprise")
    private Long idEntreprise;

    @Column(name = "entreprise", length = 50)
    private String entreprise;

    // Constructeur vide obligatoire pour JPA
    public Entreprise() {}

    // Constructeur pratique
    public Entreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    // Getters et setters
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

    @Override
    public String toString() {
        return "Entreprise{" +
                "idEntreprise=" + idEntreprise +
                ", entreprise='" + entreprise + '\'' +
                '}';
    }
}
