package com.example.back.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Backend Spring Boot fonctionne! " + new java.util.Date();
    }
    
    @GetMapping("/db-check")
    public String dbCheck() {
        return "Connexion DB OK - " + new java.util.Date();
    }
}