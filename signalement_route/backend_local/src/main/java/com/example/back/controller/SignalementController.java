package com.example.back.controller;

import com.example.back.dto.SignalementSummaryDto;
import com.example.back.dto.SignalementRecapDto;
import com.example.back.models.Signalement;
import com.example.back.repository.SignalementRepository;
import com.example.back.service.SignalementService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SignalementController {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private SignalementService signalementService;

    @GetMapping("/signalements")
    public ResponseEntity<List<Signalement>> getAllSignalements() {
        List<Signalement> signalements = signalementRepository.findAll();
        return ResponseEntity.ok(signalements);
    }

    @GetMapping("/signalements/summary")
    public ResponseEntity<List<SignalementSummaryDto>> getSignalementSummary() {
        List<SignalementSummaryDto> summary = signalementService.getSignalementSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/signalements/recapitulatif")
    public ResponseEntity<SignalementRecapDto> getSignalementRecapitulatif() {
        SignalementRecapDto recap = signalementService.getSignalementRecapitulatif();
        return ResponseEntity.ok(recap);
    }
}