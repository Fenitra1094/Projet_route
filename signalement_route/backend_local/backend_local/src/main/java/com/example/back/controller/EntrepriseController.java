package com.example.back.controller;

import com.example.back.models.Entreprise;
import com.example.back.repository.EntrepriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entreprises")
@CrossOrigin
public class EntrepriseController {

    private final EntrepriseRepository entrepriseRepository;

    @Autowired
    public EntrepriseController(EntrepriseRepository entrepriseRepository) {
        this.entrepriseRepository = entrepriseRepository;
    }

    @GetMapping
    public ResponseEntity<List<Entreprise>> listAll() {
        List<Entreprise> all = entrepriseRepository.findAll();
        return ResponseEntity.ok(all);
    }
}
