package com.ensam.projet.controller;

import com.ensam.projet.dto.request.UpdateProfileRequest;
import com.ensam.projet.dto.response.ApiResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestion des utilisateurs")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lister les utilisateurs")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(page, size), "Utilisateurs récupérés"));
    }

    @Operation(summary = "Récupérer un utilisateur")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id), "Utilisateur récupéré"));
    }

    @Operation(summary = "Mettre à jour les rôles d'un utilisateur")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(@PathVariable Long id, @RequestBody Set<String> roles) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateRoles(id, roles), "Rôles mis à jour"));
    }

    @Operation(summary = "Activer / désactiver un utilisateur")
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.toggleEnabled(id), "Statut de l'utilisateur mis à jour"));
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer mon profil")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser(), "Profil récupéré"));
    }

    @Operation(summary = "Mettre à jour mon profil")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(request.getUsername(), request.getEmail()), "Profil mis à jour"));
    }

    @Operation(summary = "Modifier le mot de passe")
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> changePassword(@Valid @RequestBody com.ensam.projet.dto.request.ChangePasswordRequest request) {
        userService.changePassword(request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Mot de passe mis à jour avec succès"));
    }
}
