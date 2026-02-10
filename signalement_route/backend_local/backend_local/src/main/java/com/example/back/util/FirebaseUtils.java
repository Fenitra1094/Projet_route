package com.example.back.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.example.back.models.*;

public class FirebaseUtils {

    public static String verifyToken(String token) throws Exception {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        return decodedToken.getUid(); // retourne l'UID Firebase de l'utilisateur
    }

     public static String register(String email, String password) throws Exception {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password);

        UserRecord userRecord =
                FirebaseAuth.getInstance().createUser(request);

        return userRecord.getUid();
    }

    public static String login(String email, String password)
            throws Exception {

        // Firebase Admin SDK NE FAIT PAS de login
        // Le login est fait côté FRONT
        // Ici on vérifie seulement que l'utilisateur existe

        UserRecord userRecord =
                FirebaseAuth.getInstance().getUserByEmail(email);

        return userRecord.getUid();
    }
}
