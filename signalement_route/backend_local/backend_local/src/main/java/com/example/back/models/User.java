package com.example.back.models;


import jakarta.persistence.*;

@Entity
@Table(name = "user_") // correspond à ta table PostgreSQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_user;

    @Column(name = "firebase_doc_id", unique = true, nullable = true)
    private String firebaseDocId;

    @Column(unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String nom;

    @Column(nullable = true)
    private Integer id_role;

    private String prenom;

    
    // replaced boolean 'bloquer' with status reference to status_blocage table
    @Column(name = "Id_status_blocage")
    private Integer idStatusBlocage;

    // Constructeur vide obligatoire pour JPA
    public User() {}

    // Constructeur pratique
    public User(String email, String password, String nom, Integer id_role) {
        this.email = email;
        this.password = password;
        this.nom = nom;
        this.id_role = id_role;
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

    public Integer getId_role() { return id_role; }
    public void setId_role(Integer id_role) { this.id_role = id_role; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Integer getIdStatusBlocage() { return idStatusBlocage; }
    public void setIdStatusBlocage(Integer idStatusBlocage) { this.idStatusBlocage = idStatusBlocage; }
    public String getFirebaseDocId() { return firebaseDocId; }
    public void setFirebaseDocId(String firebaseDocId) { this.firebaseDocId = firebaseDocId; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }
}

