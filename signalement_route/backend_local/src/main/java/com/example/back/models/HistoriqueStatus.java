package com.example.back.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historique_status")
public class HistoriqueStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_status")
    private Long idHistoriqueStatus;

    @Column(name = "date_")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "id_signalement", nullable = false)
    private Signalement signalement;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    public HistoriqueStatus() {}

    public HistoriqueStatus(LocalDate date, Signalement signalement, Status status) {
        this.date = date;
        this.signalement = signalement;
        this.status = status;
    }

    public Long getIdHistoriqueStatus() {
        return idHistoriqueStatus;
    }

    public void setIdHistoriqueStatus(Long idHistoriqueStatus) {
        this.idHistoriqueStatus = idHistoriqueStatus;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
}
