package com.ensam.projet.controller;

import com.ensam.projet.dto.request.LoginRequest;
import com.ensam.projet.dto.request.RegisterRequest;
import com.ensam.projet.dto.response.ApiResponse;
import com.ensam.projet.dto.response.AuthResponse;
import com.ensam.projet.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification et gestion des tokens")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Enregistrer un nouvel utilisateur")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request), "Inscription réussie"));
    }

    @Operation(summary = "Connexion utilisateur")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request), "Connexion réussie"));
    }

    @Operation(summary = "Renouveler le jeton d'accès")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(refreshToken), "Token rafraîchi"));
    }

    @Operation(summary = "Déconnexion")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success(null, "Déconnexion réussie"));
    }
}
