package com.example.back.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;



@Controller
@Tag(name = "Vues", description = "Contrôleur de vues JSP")
public class ViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
