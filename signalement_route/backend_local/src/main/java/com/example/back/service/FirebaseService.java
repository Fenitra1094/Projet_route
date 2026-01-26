package com.example.back.service;

import com.example.back.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    public String createUserInFirebase(User user) throws Exception {

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .setDisplayName(user.getNom());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

        return userRecord.getUid();
    }
}
