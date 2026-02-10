package com.example.cartemigration.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "signalement")
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSignalement;

    private LocalDateTime date_;
    private Double surface;
    private Double budget;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "id_quartier")
    private Quartier quartier;

    @ManyToOne
    @JoinColumn(name = "id_status")
    private Status status;

    // 🔴 Obligatoire JPA
    protected Signalement() {
    }

    // ✅ Constructeur métier principal
    public Signalement(LocalDateTime date_, Double surface, Double budget, User user, 
                       Entreprise entreprise, Quartier quartier, Status status) {
        this.date_ = date_;
        this.surface = surface;
        this.budget = budget;
        this.user = user;
        this.entreprise = entreprise;
        this.quartier = quartier;
        this.status = status;
    }

    // Constructeur complet avec id (pour tests)
    public Signalement(Long idSignalement, LocalDateTime date_, Double surface, Double budget, 
                       User user, Entreprise entreprise, Quartier quartier, Status status) {
        this.idSignalement = idSignalement;
        this.date_ = date_;
        this.surface = surface;
        this.budget = budget;
        this.user = user;
        this.entreprise = entreprise;
        this.quartier = quartier;
        this.status = status;
    }

    // Getters & Setters
    public Long getIdSignalement() {
        return idSignalement;
    }

    public void setIdSignalement(Long idSignalement) {
        this.idSignalement = idSignalement;
    }

    public LocalDateTime getDate_() {
        return date_;
    }

    public void setDate_(LocalDateTime date_) {
        this.date_ = date_;
    }

    public Double getSurface() {
        return surface;
    }

    public void setSurface(Double surface) {
        this.surface = surface;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
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
}
