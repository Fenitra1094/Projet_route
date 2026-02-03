package com.example.back.dto;

import com.example.back.models.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class SignalementResponse {
    private Long idSignalement;
    private LocalDateTime date;
    private BigDecimal surface;
    private BigDecimal budget;
    private User user;
    private Quartier quartier;
    private Entreprise entreprise;
    private Status status;

    // Getters et setters
    public Long getIdSignalement() { return idSignalement; }
    public void setIdSignalement(Long idSignalement) { this.idSignalement = idSignalement; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public BigDecimal getSurface() { return surface; }
    public void setSurface(BigDecimal surface) { this.surface = surface; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Quartier getQuartier() { return quartier; }
    public void setQuartier(Quartier quartier) { this.quartier = quartier; }

    public Entreprise getEntreprise() { return entreprise; }
    public void setEntreprise(Entreprise entreprise) { this.entreprise = entreprise; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}