package com.example.back.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_status")
public class HistoriqueStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_status")
    private Long idHistoriqueStatus;

    @Column(name = "date_")
    private LocalDateTime dateChangement;

    @ManyToOne
    @JoinColumn(name = "id_signalement", nullable = false)
    private Signalement signalement;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    // Constructeur vide obligatoire pour JPA
    public HistoriqueStatus() {}

    // Constructeur pratique
    public HistoriqueStatus(LocalDateTime dateChangement, Signalement signalement, Status status) {
        this.dateChangement = dateChangement;
        this.signalement = signalement;
        this.status = status;
    }

    // Getters et setters
    public Long getIdHistoriqueStatus() {
        return idHistoriqueStatus;
    }

    public void setIdHistoriqueStatus(Long idHistoriqueStatus) {
        this.idHistoriqueStatus = idHistoriqueStatus;
    }

    public LocalDateTime getDateChangement() {
        return dateChangement;
    }

    public void setDateChangement(LocalDateTime dateChangement) {
        this.dateChangement = dateChangement;
    }

    public Signalement getSignalement() {
        return signalement;
    }

    public void setSignalement(Signalement signalement) {
        this.signalement = signalement;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "HistoriqueStatus{" +
                "idHistoriqueStatus=" + idHistoriqueStatus +
                ", dateChangement=" + dateChangement +
                ", signalement=" + signalement +
                ", status=" + status +
                '}';
    }
}