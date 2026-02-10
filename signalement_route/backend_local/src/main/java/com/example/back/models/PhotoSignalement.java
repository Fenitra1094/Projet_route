package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "photo_signalement")
public class PhotoSignalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_photo_signalement")
    private Long idPhotoSignalement;

    @Column(name = "libelle", length = 50)
    private String libelle;

    @ManyToOne
    @JoinColumn(name = "id_signalement")
    private Signalement signalement;

    // Constructeur vide obligatoire pour JPA
    public PhotoSignalement() {}

    // Constructeur pratique
    public PhotoSignalement(String libelle, Signalement signalement) {
        this.libelle = libelle;
        this.signalement = signalement;
    }

    // Getters et setters
    public Long getIdPhotoSignalement() {
        return idPhotoSignalement;
    }

    public void setIdPhotoSignalement(Long idPhotoSignalement) {
        this.idPhotoSignalement = idPhotoSignalement;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Signalement getSignalement() {
        return signalement;
    }

    public void setSignalement(Signalement signalement) {
        this.signalement = signalement;
    }

    @Override
    public String toString() {
        return "PhotoSignalement{" +
                "idPhotoSignalement=" + idPhotoSignalement +
                ", libelle='" + libelle + '\'' +
                ", signalement=" + (signalement != null ? signalement.getIdSignalement() : null) +
                '}';
    }
}
