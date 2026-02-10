package com.example.back.controller;

import com.example.back.dto.UpdateSignalementDTO;
import com.example.back.models.Entreprise;
import com.example.back.models.Quartier;
import com.example.back.models.Signalement;
import com.example.back.models.Status;
import com.example.back.models.User;
import com.example.back.repository.EntrepriseRepository;
import com.example.back.repository.SignalementRepository;
import com.example.back.repository.StatusRepository;
import com.example.back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/signalements")
@CrossOrigin
public class SignalementController {

    private final SignalementRepository signalementRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;
    private final EntrepriseRepository entrepriseRepository;

    @Autowired
    public SignalementController(SignalementRepository signalementRepository, StatusRepository statusRepository, 
                                  UserRepository userRepository, EntrepriseRepository entrepriseRepository) {
        this.signalementRepository = signalementRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
        this.entrepriseRepository = entrepriseRepository;
    }

    // List all signalements
    @GetMapping
    public ResponseEntity<List<Signalement>> listAll() {
        List<Signalement> all = signalementRepository.findAll();
        return ResponseEntity.ok(all);
    }

    // Get single signalement
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") Long id) {
        Optional<Signalement> s = signalementRepository.findById(id);
        return s.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

        

    // Update entire signalement by id (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Signalement payload) {
        Optional<Signalement> existing = signalementRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        Signalement s = existing.get();

        // Update simple fields
        s.setDate(payload.getDate());
        s.setSurface(payload.getSurface());
        s.setBudget(payload.getBudget());

        // Update user if provided
        if (payload.getUser() != null && payload.getUser().getId_user() != null) {
            Optional<User> u = userRepository.findById(payload.getUser().getId_user());
            u.ifPresent(s::setUser);
        }

        // Update entreprise/quartier/status if provided (we attach by id only if present)
        if (payload.getEntreprise() != null && payload.getEntreprise().getIdEntreprise() != null) {
            s.setEntreprise(payload.getEntreprise());
        }
        if (payload.getQuartier() != null && payload.getQuartier() != null) {
            s.setQuartier(payload.getQuartier());
        }
        if (payload.getStatus() != null && payload.getStatus().getIdStatus() != null) {
            Optional<Status> st = statusRepository.findById(payload.getStatus().getIdStatus());
            st.ifPresent(s::setStatus);
        }

        Signalement saved = signalementRepository.save(s);
        return ResponseEntity.ok(saved);
    }

    // Change only the status of a signalement
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestParam("statusId") Long statusId) {
        Optional<Signalement> existing = signalementRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        Optional<Status> st = statusRepository.findById(statusId);
        if (st.isEmpty()) return ResponseEntity.badRequest().body("status introuvable");

        Signalement s = existing.get();
        s.setStatus(st.get());
        signalementRepository.save(s);
        return ResponseEntity.ok(s);
    }

    // Partial update for budget, surface, and entreprise
    @PatchMapping("/{id}/edit")
    public ResponseEntity<?> partialUpdate(@PathVariable Long id, @RequestBody UpdateSignalementDTO dto) {
        Optional<Signalement> existing = signalementRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        
        Signalement s = existing.get();
        
        // Update budget if provided
        if (dto.getBudget() != null) {
            s.setBudget(dto.getBudget());
        }
        
        // Update surface if provided
        if (dto.getSurface() != null) {
            s.setSurface(dto.getSurface());
        }
        
        // Update entreprise if provided
        if (dto.getEntrepriseId() != null) {
            Optional<Entreprise> entreprise = entrepriseRepository.findById(dto.getEntrepriseId());
            if (entreprise.isEmpty()) {
                return ResponseEntity.badRequest().body("Entreprise introuvable");
            }
            s.setEntreprise(entreprise.get());
        }
        
        Signalement saved = signalementRepository.save(s);
        return ResponseEntity.ok(saved);
    }

}
