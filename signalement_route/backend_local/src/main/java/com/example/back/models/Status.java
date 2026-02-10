package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_status")
    private Long idStatus;

    @Column(name = "libelle", length = 50)
    private String libelle;

    // Constructeur vide obligatoire pour JPA
    public Status() {}

    // Constructeur pratique
    public Status(String libelle) {
        this.libelle = libelle;
    }

    // Getters et setters
    public Long getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Long idStatus) {
        this.idStatus = idStatus;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "Status{" +
                "idStatus=" + idStatus +
                ", libelle='" + libelle + '\'' +
                '}';
    }
}
