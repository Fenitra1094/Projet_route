package com.example.cartemigration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignalementDTO {
    
    @JsonProperty("id")
    private Long idSignalement;
    
    @JsonProperty("titre")
    private String titre;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("statut")
    private String statut;
    
    @JsonProperty("surface_m2")
    private Double surfaceM2;
    
    @JsonProperty("budget")
    private Double budget;
    
    @JsonProperty("date_creation")
    private LocalDateTime dateCreation;
    
    @JsonProperty("entreprise_nom")
    private String entrepriseNom;
    
    @JsonProperty("id_entreprise")
    private Long idEntreprise;
    
    @JsonProperty("user_nom")
    private String userNom;
    
    @JsonProperty("province_nom")
    private String provinceNom;
}
