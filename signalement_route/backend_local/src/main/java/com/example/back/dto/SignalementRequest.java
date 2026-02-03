package com.example.back.dto;

import java.math.BigDecimal;

public class SignalementRequest {
    private Long idQuartier;
    private Long idEntreprise;
    private BigDecimal surface;
    private BigDecimal budget;
    private String description;

    // Constructeurs
    public SignalementRequest() {}

    public SignalementRequest(Long idQuartier, Long idEntreprise, 
                             BigDecimal surface, BigDecimal budget, String description) {
        this.idQuartier = idQuartier;
        this.idEntreprise = idEntreprise;
        this.surface = surface;
        this.budget = budget;
        this.description = description;
    }

    // Getters et setters
    public Long getIdQuartier() { return idQuartier; }
    public void setIdQuartier(Long idQuartier) { this.idQuartier = idQuartier; }

    public Long getIdEntreprise() { return idEntreprise; }
    public void setIdEntreprise(Long idEntreprise) { this.idEntreprise = idEntreprise; }

    public BigDecimal getSurface() { return surface; }
    public void setSurface(BigDecimal surface) { this.surface = surface; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}