package com.example.back.service;

import com.example.back.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirebaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    public String createUserInFirebase(User user) throws FirebaseAuthException {
        try {
            logger.info("Création utilisateur Firebase pour: " + user.getEmail());
            
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(user.getEmail())
                    .setEmailVerified(false) // À vérifier par email plus tard
                    .setPassword(user.getPassword())
                    .setDisplayName(user.getNom() + " " + user.getPrenom())
                    .setDisabled(false);
            
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            
            logger.info("Utilisateur Firebase créé avec UID: " + userRecord.getUid());
            return userRecord.getUid();
            
        } catch (FirebaseAuthException e) {
            logger.error("Erreur Firebase Auth: " + e.getErrorCode() + " - " + e.getMessage());
            throw e;
        }
    }
    
    public UserRecord getUserByUid(String uid) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().getUser(uid);
    }
    
    public UserRecord getUserByEmail(String email) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().getUserByEmail(email);
    }
    
    public void deleteUser(String uid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
    }
}