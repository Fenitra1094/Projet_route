package com.example.back.service;

import com.example.back.models.*;
import com.example.back.repository.*;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User inscrire(User utilisateur) {
        if (repository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new RuntimeException("Utilisateur déjà inscrit");
        }
        return repository.save(utilisateur);
    }
}
