package com.example.back.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("Initialisation de Firebase...");
                
                // Charger le fichier de configuration
                InputStream serviceAccount = 
                    new ClassPathResource("firebase-service-account.json").getInputStream();
                
                // Lire le fichier JSON pour obtenir l'email du client
                String jsonContent = new String(serviceAccount.readAllBytes());
                serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream(); // Réinitialiser le stream
                
                // Extraire l'email du client depuis le JSON
                String clientEmail = extractClientEmail(jsonContent);
                
                // Configurer Firebase
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
                
                // Initialiser l'application
                FirebaseApp.initializeApp(options);
                
                System.out.println("Firebase initialisé avec succès!");
                System.out.println("Nom du projet: " + options.getProjectId());
                System.out.println("Client Email: " + clientEmail);
                
            } else {
                System.out.println("Firebase déjà initialisé");
            }
        } catch (IOException e) {
            System.err.println("ERREUR: Impossible d'initialiser Firebase");
            System.err.println("Vérifiez que le fichier firebase-service-account.json existe dans src/main/resources/");
            e.printStackTrace();
            throw new RuntimeException("Erreur d'initialisation Firebase", e);
        }
    }
    
    private String extractClientEmail(String jsonContent) {
        try {
            // Simple extraction de l'email depuis le JSON
            String emailKey = "\"client_email\":\"";
            int startIndex = jsonContent.indexOf(emailKey);
            if (startIndex != -1) {
                startIndex += emailKey.length();
                int endIndex = jsonContent.indexOf("\"", startIndex);
                return jsonContent.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            System.err.println("Impossible d'extraire l'email client: " + e.getMessage());
        }
        return "Email non disponible";
    }
}