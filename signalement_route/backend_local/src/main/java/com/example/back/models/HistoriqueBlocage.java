package com.example.back.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historique_blocage")
public class HistoriqueBlocage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_blocage")
    private Long idHistoriqueBlocage;

    @Column(name = "date_")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "id_status_blocage", nullable = false)
    private StatusBlocage statusBlocage;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    // Constructeur vide obligatoire pour JPA
    public HistoriqueBlocage() {}

    // Constructeur pratique
    public HistoriqueBlocage(LocalDate date, StatusBlocage statusBlocage, User user) {
        this.date = date;
        this.statusBlocage = statusBlocage;
        this.user = user;
    }

    // Getters et setters
    public Long getIdHistoriqueBlocage() {
        return idHistoriqueBlocage;
    }

    public void setIdHistoriqueBlocage(Long idHistoriqueBlocage) {
        this.idHistoriqueBlocage = idHistoriqueBlocage;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    @Override
    public String toString() {
        return "HistoriqueBlocage{" +
                "idHistoriqueBlocage=" + idHistoriqueBlocage +
                ", date=" + date +
                ", statusBlocage=" + statusBlocage +
                ", user=" + user +
                '}';
    }
}
