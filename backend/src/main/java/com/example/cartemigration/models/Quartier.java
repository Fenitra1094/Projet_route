package com.example.cartemigration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quartier")
public class Quartier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idQuartier;

    private String quartier;
    private Double positionX;
    private Double positionY;

    @ManyToOne
    @JoinColumn(name = "id_province")
    private Province province;

    // 🔴 Obligatoire JPA
    protected Quartier() {
    }

    // ✅ Constructeur métier principal
    public Quartier(String quartier, Double positionX, Double positionY, Province province) {
        this.quartier = quartier;
        this.positionX = positionX;
        this.positionY = positionY;
        this.province = province;
    }

    // Constructeur complet avec id (pour tests)
    public Quartier(Long idQuartier, String quartier, Double positionX, Double positionY, Province province) {
        this.idQuartier = idQuartier;
        this.quartier = quartier;
        this.positionX = positionX;
        this.positionY = positionY;
        this.province = province;
    }

    // Getters & Setters
    public Long getIdQuartier() {
        return idQuartier;
    }

    public void setIdQuartier(Long idQuartier) {
        this.idQuartier = idQuartier;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }
}
