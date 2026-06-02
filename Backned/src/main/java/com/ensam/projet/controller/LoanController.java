package com.ensam.projet.controller;

import com.ensam.projet.dto.request.LoanRequest;
import com.ensam.projet.dto.response.ApiResponse;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.entity.LoanStatus;
import com.ensam.projet.service.interfaces.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Gestion des emprunts")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Lister les emprunts")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LoanStatus status,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getAllLoans(page, size, status, userId), "Liste récupérée"));
    }

    @Operation(summary = "Récupérer un emprunt")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoan(@PathVariable Long id, Authentication authentication) {
        LoanResponse response = loanService.getLoanById(id);
        if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER") || auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(ApiResponse.success(response, "Emprunt récupéré"));
        }
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        if (!response.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Accès refusé");
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Emprunt récupéré"));
    }

    @Operation(summary = "Créer un emprunt")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<LoanResponse>> createLoan(@Valid @RequestBody LoanRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(loanService.createLoan(request), "Emprunt créé"));
    }

    @Operation(summary = "Mettre à jour un emprunt")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LoanResponse>> updateLoan(@PathVariable Long id, @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loanService.updateLoan(id, request), "Emprunt mis à jour"));
    }

    @Operation(summary = "Marquer un emprunt comme retourné")
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<LoanResponse>> returnLoan(@PathVariable Long id, @RequestParam String returnedBy) {
        return ResponseEntity.ok(ApiResponse.success(loanService.returnLoan(id, returnedBy), "Emprunt retourné"));
    }

    @Operation(summary = "Supprimer un emprunt")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lister mes emprunts")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getMyLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getMyLoans(page, size), "Mes emprunts récupérés"));
    }
}
