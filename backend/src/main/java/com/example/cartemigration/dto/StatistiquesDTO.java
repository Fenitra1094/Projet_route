package com.example.cartemigration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesDTO {
    
    @JsonProperty("nombre_problemes")
    private Integer nombreProblemes;
    
    @JsonProperty("surface_totale_m2")
    private Double surfaceTotaleMetre;
    
    @JsonProperty("budget_total")
    private Double budgetTotal;
    
    @JsonProperty("pourcentage_termines")
    private Double pourcentageTermines;
}
