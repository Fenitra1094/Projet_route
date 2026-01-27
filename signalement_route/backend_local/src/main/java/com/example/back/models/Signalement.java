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

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "id_quartier", nullable = false)
    private Quartier quartier;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    // Constructeur vide obligatoire pour JPA
    public Signalement() {}

    // Constructeur pratique
    public Signalement(LocalDateTime date, BigDecimal surface, BigDecimal budget, 
                       User user, Entreprise entreprise, Quartier quartier, Status status) {
        this.date = date;
        this.surface = surface;
        this.budget = budget;
        this.user = user;
        this.entreprise = entreprise;
        this.quartier = quartier;
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

    public Quartier getQuartier() {
        return quartier;
    }

    public void setQuartier(Quartier quartier) {
        this.quartier = quartier;
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
                ", user=" + user +
                ", entreprise=" + entreprise +
                ", quartier=" + quartier +
                ", status=" + status +
                '}';
    }
}
