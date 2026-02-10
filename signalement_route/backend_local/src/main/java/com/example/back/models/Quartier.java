package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "quartier")
public class Quartier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_quartier;

    @Column(nullable = false)
    private String quartier;

    @Column(nullable = false)
    private Double positionX;

    @Column(nullable = false)
    private Double positionY;

    @ManyToOne
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    public Quartier() {}

    public Quartier(String quartier, Double positionX, Double positionY, Province province) {
        this.quartier = quartier;
        this.positionX = positionX;
        this.positionY = positionY;
        this.province = province;
    }

    public Long getId_quartier() {
        return id_quartier;
    }

    public void setId_quartier(Long id_quartier) {
        this.id_quartier = id_quartier;
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