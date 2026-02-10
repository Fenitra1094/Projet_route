package com.example.back.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "signalement")
public class Signalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signalement")
    private Long idSignalement;

    @Column(name = "date_")
    private LocalDateTime date;

    @Column(name = "surface", precision = 15, scale = 2)
    private BigDecimal surface;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "longitude")
    private Integer longitude;

    @Column(name = "latitude")
    private Integer latitude;

    @Column(name = "firebase_doc_id", length = 50)
    private String firebaseDocId;

    @Column(name = "quartier", length = 50)
    private String quartier;

    @ManyToOne
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    // Constructeur vide obligatoire pour JPA
    public Signalement() {}

    // Constructeur pratique
    public Signalement(LocalDateTime date, BigDecimal surface, BigDecimal budget, 
                       Integer longitude, Integer latitude, String firebaseDocId, 
                       String quartier, Province province, User user, 
                       Entreprise entreprise, Status status) {
        this.date = date;
        this.surface = surface;
        this.budget = budget;
        this.longitude = longitude;
        this.latitude = latitude;
        this.firebaseDocId = firebaseDocId;
        this.quartier = quartier;
        this.province = province;
        this.user = user;
        this.entreprise = entreprise;
        this.status = status;
    }

    // Getters et setters
    public Long getIdSignalement() {
        return idSignalement;
    }

    public void setIdSignalement(Long idSignalement) {
        this.idSignalement = idSignalement;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getSurface() {
        return surface;
    }

    public void setSurface(BigDecimal surface) {
        this.surface = surface;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public void setLongitude(Integer longitude) {
        this.longitude = longitude;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public void setLatitude(Integer latitude) {
        this.latitude = latitude;
    }

    public String getFirebaseDocId() {
        return firebaseDocId;
    }

    public void setFirebaseDocId(String firebaseDocId) {
        this.firebaseDocId = firebaseDocId;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Signalement{" +
                "idSignalement=" + idSignalement +
                ", date=" + date +
                ", surface=" + surface +
                ", budget=" + budget +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", firebaseDocId='" + firebaseDocId + '\'' +
                ", quartier='" + quartier + '\'' +
                ", province=" + province +
                ", user=" + user +
                ", entreprise=" + entreprise +
                ", status=" + status +
                '}';
    }
}
