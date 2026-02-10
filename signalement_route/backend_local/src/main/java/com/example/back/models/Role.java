package com.example.back.models;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_role;

    @Column(nullable = false, unique = true)
    private String libelle;

    public Role() {}
    public Role(String libelle) {
        this.libelle = libelle;
    }

    public Long getId_role() {
        return id_role;
    }
    public void setId_role(Long id_role) {
        this.id_role = id_role;
    }
    public String getLibelle() {
        return libelle;
    }
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}