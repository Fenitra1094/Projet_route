package com.example.back.controller;

import com.example.back.models.Role;
import com.example.back.repository.RoleRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/roles")
    @Operation(
        summary = "Lister tous les rôles",
        description = "Récupère la liste complète des rôles utilisateur définis dans le système (ex: UTILISATEUR, MANAGER)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des rôles récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Role.class)))
    })
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }
}
