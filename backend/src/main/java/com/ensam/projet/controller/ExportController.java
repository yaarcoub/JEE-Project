package com.ensam.projet.controller;

import com.ensam.projet.dto.response.ApiResponse;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.service.interfaces.BookService;
import com.ensam.projet.service.interfaces.ExportService;
import com.ensam.projet.service.interfaces.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "Export PDF / Excel")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final BookService bookService;
    private final LoanService loanService;

    @Operation(summary = "Exporter les livres en PDF")
    @GetMapping("/pdf/books")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> exportBooksPdf() {
        List<BookResponse> books = bookService.getAllBooks(0, 1000, "title", "asc", null, null).getContent();
        byte[] pdf = exportService.exportBooksToPdf(books.stream().map(book -> {
            com.ensam.projet.entity.Book entity = new com.ensam.projet.entity.Book();
            entity.setId(book.getId());
            entity.setTitle(book.getTitle());
            entity.setAuthor(book.getAuthor());
            entity.setIsbn(book.getIsbn());
            entity.setStock(book.getStock());
            entity.setCategories(book.getCategories() == null ? null : book.getCategories().stream().map(categoryResponse -> {
                com.ensam.projet.entity.Category category = new com.ensam.projet.entity.Category();
                category.setName(categoryResponse.getName());
                return category;
            }).collect(java.util.stream.Collectors.toSet()));
            entity.setCreatedAt(book.getCreatedAt());
            return entity;
        }).toList());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Exporter les livres en Excel")
    @GetMapping("/excel/books")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> exportBooksExcel() {
        List<BookResponse> books = bookService.getAllBooks(0, 1000, "title", "asc", null, null).getContent();
        byte[] excel = exportService.exportBooksToExcel(books.stream().map(book -> {
            com.ensam.projet.entity.Book entity = new com.ensam.projet.entity.Book();
            entity.setId(book.getId());
            entity.setTitle(book.getTitle());
            entity.setAuthor(book.getAuthor());
            entity.setIsbn(book.getIsbn());
            entity.setStock(book.getStock());
            entity.setCategories(book.getCategories() == null ? null : book.getCategories().stream().map(categoryResponse -> {
                com.ensam.projet.entity.Category category = new com.ensam.projet.entity.Category();
                category.setName(categoryResponse.getName());
                return category;
            }).collect(java.util.stream.Collectors.toSet()));
            entity.setCreatedAt(book.getCreatedAt());
            return entity;
        }).toList());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @Operation(summary = "Exporter les emprunts en PDF")
    @GetMapping("/pdf/loans")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> exportLoansPdf() {
        List<LoanResponse> loans = loanService.getAllLoans(0, 1000, null, null).getContent();
        byte[] pdf = exportService.exportLoansToPdf(loans.stream().map(loanResponse -> {
            com.ensam.projet.entity.Loan loan = new com.ensam.projet.entity.Loan();
            loan.setId(loanResponse.getId());
            loan.setStatus(loanResponse.getStatus());
            loan.setLoanDate(loanResponse.getLoanDate());
            loan.setExpectedReturnDate(loanResponse.getExpectedReturnDate());
            com.ensam.projet.entity.Book book = new com.ensam.projet.entity.Book();
            book.setTitle(loanResponse.getBook().getTitle());
            loan.setBook(book);
            com.ensam.projet.entity.User user = new com.ensam.projet.entity.User();
            user.setUsername(loanResponse.getUser().getUsername());
            loan.setUser(user);
            return loan;
        }).toList());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loans.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
