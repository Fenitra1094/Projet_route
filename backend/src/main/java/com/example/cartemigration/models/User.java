package com.example.cartemigration.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    private String nom;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private Role role;

    // 🔴 Obligatoire JPA
    protected User() {
    }

    // ✅ Constructeur métier principal
    public User(String nom, Role role) {
        this.nom = nom;
        this.role = role;
    }

    // Constructeur complet avec id (pour tests)
    public User(Long idUser, String nom, Role role) {
        this.idUser = idUser;
        this.nom = nom;
        this.role = role;
    }

    // Getters & Setters
    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
