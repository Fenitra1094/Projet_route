package com.example.back.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "user_")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;

    @Column(name = "firebase_uid", unique = true, nullable = false)
    private String firebaseUid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nom;

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    private Role role;

    private String prenom;
    private boolean synced;

    // Ajoutez cette relation pour l'historique de blocage
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistoriqueBlocage> historiqueBlocages;

    // Constructeurs
    public User() {}

    public User(String email, String password, String nom, Role role) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.role = role;
    }

    // Getters et setters
    public Long getId_user() { return id_user; }
    public void setId_user(Long id_user) { this.id_user = id_user; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public List<HistoriqueBlocage> getHistoriqueBlocages() { return historiqueBlocages; }
    public void setHistoriqueBlocages(List<HistoriqueBlocage> historiqueBlocages) { 
        this.historiqueBlocages = historiqueBlocages; 
    }
}