package com.example.back.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        System.out.println("🚀 Initialisation Firebase...");
        
        try {
            // Vérifier si déjà initialisé
            if (FirebaseApp.getApps().isEmpty()) {
                // Charger le fichier
                InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase/serviceAccountKey.json");
                
                if (serviceAccount == null) {
                    throw new RuntimeException("❌ serviceAccountKey.json introuvable dans classpath:firebase/");
                }
                
                System.out.println("✅ Fichier serviceAccountKey.json trouvé");
                
                // Initialiser Firebase
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
                
                FirebaseApp.initializeApp(options);
                System.out.println("✅ FirebaseApp initialisé avec succès");
            } else {
                System.out.println("ℹ️ Firebase déjà initialisé");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation Firebase: " + e.getMessage());
            throw new RuntimeException("Échec initialisation Firebase", e);
        }
    }

    @Bean
    public Firestore firestore() {
        try {
            System.out.println("🔧 Création bean Firestore...");
            
            // Vérifier que Firebase est initialisé
            if (FirebaseApp.getApps().isEmpty()) {
                throw new IllegalStateException("FirebaseApp non initialisé! Appelez d'abord initFirebase()");
            }
            
            // Obtenir Firestore depuis Firebase
            Firestore firestore = FirestoreClient.getFirestore();
            
            // Tester la connexion
            System.out.println("✅ Bean Firestore créé avec succès");
            return firestore;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur création bean Firestore: " + e.getMessage());
            throw new RuntimeException("Impossible de créer Firestore bean", e);
        }
    }
}