package com.example.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;

@SpringBootApplication
public class GestionApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GestionApplication.class);
    }

    public static void main(String[] args) {
        // 🔹 Initialisation Firebase
        try {
            FileInputStream serviceAccount = new FileInputStream("chemin/vers/serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialisé ✅");
        } catch (Exception e) {
            System.out.println("Impossible d'initialiser Firebase : " + e.getMessage());
        }

        SpringApplication.run(GestionApplication.class, args);
    }
}
