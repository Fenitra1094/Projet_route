package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "parametre")
public class Parametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "duree_session")
    private Integer dureeSession;

    @Column(name = "nombre_tentative")
    private Integer nombreTentative;

    public Parametre() {}

    public Parametre(Integer dureeSession, Integer nombreTentative) {
        this.dureeSession = dureeSession;
        this.nombreTentative = nombreTentative;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDureeSession() { return dureeSession; }
    public void setDureeSession(Integer dureeSession) { this.dureeSession = dureeSession; }

    public Integer getNombreTentative() { return nombreTentative; }
    public void setNombreTentative(Integer nombreTentative) { this.nombreTentative = nombreTentative; }
}
