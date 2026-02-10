package com.example.back.controller;

import com.example.back.models.Status;
import com.example.back.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status")
@CrossOrigin
public class StatusController {

    private final StatusRepository statusRepository;

    @Autowired
    public StatusController(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @GetMapping
    public ResponseEntity<List<Status>> listAll() {
        List<Status> all = statusRepository.findAll();
        return ResponseEntity.ok(all);
    }
}
