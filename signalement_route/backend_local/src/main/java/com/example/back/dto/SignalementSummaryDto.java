package com.example.back.dto;

public class SignalementSummaryDto {
    private Long idSignalement;
    private String date;
    private String surface;
    private String budget;
    private String userName;
    private String entrepriseName;
    private String quartierName;
    private String statusLibelle;

    public SignalementSummaryDto(Long idSignalement, String date, String surface, String budget,
                                 String userName, String entrepriseName, String quartierName, String statusLibelle) {
        this.idSignalement = idSignalement;
        this.date = date;
        this.surface = surface;
        this.budget = budget;
        this.userName = userName;
        this.entrepriseName = entrepriseName;
        this.quartierName = quartierName;
        this.statusLibelle = statusLibelle;
    }

    // Getters
    public Long getIdSignalement() { return idSignalement; }
    public String getDate() { return date; }
    public String getSurface() { return surface; }
    public String getBudget() { return budget; }
    public String getUserName() { return userName; }
    public String getEntrepriseName() { return entrepriseName; }
    public String getQuartierName() { return quartierName; }
    public String getStatusLibelle() { return statusLibelle; }
}