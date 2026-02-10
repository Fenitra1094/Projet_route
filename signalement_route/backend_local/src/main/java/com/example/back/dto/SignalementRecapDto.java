package com.example.back.dto;

import java.math.BigDecimal;
import java.util.List;

public class SignalementRecapDto {
    private Long nombrePointsSignales;
    private BigDecimal surfaceTotalTraitee;
    private Double tauxAvancement;
    private BigDecimal budgetTotalAlloue;
    private List<SignalementParArrondissementDto> detailsParArrondissement;

    public SignalementRecapDto() {}

    public SignalementRecapDto(Long nombrePointsSignales, BigDecimal surfaceTotalTraitee, 
                               Double tauxAvancement, BigDecimal budgetTotalAlloue, 
                               List<SignalementParArrondissementDto> detailsParArrondissement) {
        this.nombrePointsSignales = nombrePointsSignales;
        this.surfaceTotalTraitee = surfaceTotalTraitee;
        this.tauxAvancement = tauxAvancement;
        this.budgetTotalAlloue = budgetTotalAlloue;
        this.detailsParArrondissement = detailsParArrondissement;
    }

    public Long getNombrePointsSignales() {
        return nombrePointsSignales;
    }

    public void setNombrePointsSignales(Long nombrePointsSignales) {
        this.nombrePointsSignales = nombrePointsSignales;
    }

    public BigDecimal getSurfaceTotalTraitee() {
        return surfaceTotalTraitee;
    }

    public void setSurfaceTotalTraitee(BigDecimal surfaceTotalTraitee) {
        this.surfaceTotalTraitee = surfaceTotalTraitee;
    }

    public Double getTauxAvancement() {
        return tauxAvancement;
    }

    public void setTauxAvancement(Double tauxAvancement) {
        this.tauxAvancement = tauxAvancement;
    }

    public BigDecimal getBudgetTotalAlloue() {
        return budgetTotalAlloue;
    }

    public void setBudgetTotalAlloue(BigDecimal budgetTotalAlloue) {
        this.budgetTotalAlloue = budgetTotalAlloue;
    }

    public List<SignalementParArrondissementDto> getDetailsParArrondissement() {
        return detailsParArrondissement;
    }

    public void setDetailsParArrondissement(List<SignalementParArrondissementDto> detailsParArrondissement) {
        this.detailsParArrondissement = detailsParArrondissement;
    }
}
