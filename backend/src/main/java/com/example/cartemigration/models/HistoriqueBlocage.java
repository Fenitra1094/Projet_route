package com.example.cartemigration.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "historique_blocage")
public class HistoriqueBlocage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistoriqueBlocage;

    private LocalDate date_;

    @ManyToOne
    @JoinColumn(name = "id_status_blocage")
    private StatusBlocage statusBlocage;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    // 🔴 Obligatoire JPA
    protected HistoriqueBlocage() {
    }

    // ✅ Constructeur métier principal
    public HistoriqueBlocage(LocalDate date_, StatusBlocage statusBlocage, User user) {
        this.date_ = date_;
        this.statusBlocage = statusBlocage;
        this.user = user;
    }

    // Constructeur complet avec id (pour tests)
    public HistoriqueBlocage(Long idHistoriqueBlocage, LocalDate date_, StatusBlocage statusBlocage, User user) {
        this.idHistoriqueBlocage = idHistoriqueBlocage;
        this.date_ = date_;
        this.statusBlocage = statusBlocage;
        this.user = user;
    }

    // Getters & Setters
    public Long getIdHistoriqueBlocage() {
        return idHistoriqueBlocage;
    }

    public void setIdHistoriqueBlocage(Long idHistoriqueBlocage) {
        this.idHistoriqueBlocage = idHistoriqueBlocage;
    }

    public LocalDate getDate_() {
        return date_;
    }

    public void setDate_(LocalDate date_) {
        this.date_ = date_;
    }

    public StatusBlocage getStatusBlocage() {
        return statusBlocage;
    }

    public void setStatusBlocage(StatusBlocage statusBlocage) {
        this.statusBlocage = statusBlocage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
