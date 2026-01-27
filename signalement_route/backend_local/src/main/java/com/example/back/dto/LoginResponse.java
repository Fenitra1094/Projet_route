package com.example.back.dto;

public class LoginResponse {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    private String firebaseUid;
    
    public LoginResponse() {}
    
    public LoginResponse(Long id, String email, String nom, String prenom, 
                        String role, String firebaseUid) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.firebaseUid = firebaseUid;
    }
    
    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }
}