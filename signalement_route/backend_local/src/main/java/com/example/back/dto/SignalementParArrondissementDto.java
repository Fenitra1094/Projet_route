package com.example.back.dto;

import java.math.BigDecimal;

public class SignalementParArrondissementDto {
    private String arrondissement;
    private Long points;
    private BigDecimal surface;
    private Double avancement;
    private BigDecimal budget;

    public SignalementParArrondissementDto() {}

    public SignalementParArrondissementDto(String arrondissement, Long points, 
                                          BigDecimal surface, Double avancement, BigDecimal budget) {
        this.arrondissement = arrondissement;
        this.points = points;
        this.surface = surface;
        this.avancement = avancement;
        this.budget = budget;
    }

    public String getArrondissement() {
        return arrondissement;
    }

    public void setArrondissement(String arrondissement) {
        this.arrondissement = arrondissement;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public BigDecimal getSurface() {
        return surface;
    }

    public void setSurface(BigDecimal surface) {
        this.surface = surface;
    }

    public Double getAvancement() {
        return avancement;
    }

    public void setAvancement(Double avancement) {
        this.avancement = avancement;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
}
