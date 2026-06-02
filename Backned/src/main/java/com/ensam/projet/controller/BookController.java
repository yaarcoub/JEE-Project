package com.ensam.projet.controller;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.ApiResponse;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.service.interfaces.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Gestion des livres")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Lister les livres")
    @ApiResponses({})
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getAllBooks(page, size, sort, direction, search, categoryId), "Liste récupérée"));
    }

    @Operation(summary = "Récupérer un livre")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookResponse>> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBookById(id), "Livre récupéré"));
    }

    @Operation(summary = "Créer un livre")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.success(bookService.createBook(request), "Livre créé"));
    }

    @Operation(summary = "Mettre à jour un livre")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bookService.updateBook(id, request), "Livre mis à jour"));
    }

    @Operation(summary = "Supprimer un livre")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assigner des catégories à un livre")
    @PostMapping("/{id}/categories")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<BookResponse>> assignCategories(@PathVariable Long id, @RequestBody Set<Long> categoryIds) {
        return ResponseEntity.ok(ApiResponse.success(bookService.assignCategories(id, categoryIds), "Catégories assignées"));
    }

    @Operation(summary = "Récupérer les emprunts d'un livre")
    @GetMapping("/{id}/loans")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getBookLoans(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBookLoans(id, page, size), "Emprunts récupérés"));
    }
}
