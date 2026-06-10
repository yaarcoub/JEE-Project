package com.ensam.projet.controller;

import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.service.interfaces.BookService;
import com.ensam.projet.service.interfaces.ExportService;
import com.ensam.projet.service.interfaces.LoanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:exportctrltestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
    "spring.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportBooksPdfAsAdmin() throws Exception {
        // Mock profond pour passer au travers des transformations de flux (Streams)
        BookResponse mockBook = mock(BookResponse.class);
        CategoryResponse mockCategory = mock(CategoryResponse.class);
        
        when(mockBook.getId()).thenReturn(1L);
        when(mockBook.getTitle()).thenReturn("Design Patterns");
        when(mockBook.getAuthor()).thenReturn("Erich Gamma");
        when(mockBook.getIsbn()).thenReturn("978-0201633610");
        when(mockBook.getStock()).thenReturn(5);
        when(mockBook.getCategories()).thenReturn(Collections.singleton(mockCategory));
        when(mockCategory.getName()).thenReturn("Informatique");

        PagedResponse<BookResponse> mockPage = mock(PagedResponse.class);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(mockBook));

        when(bookService.getAllBooks(anyInt(), anyInt(), anyString(), anyString(), any(), any())).thenReturn(mockPage);
        when(exportService.exportBooksToPdf(anyList())).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/export/pdf/books"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldExportBooksExcelAsManager() throws Exception {
        BookResponse mockBook = mock(BookResponse.class);
        when(mockBook.getId()).thenReturn(2L);
        when(mockBook.getCategories()).thenReturn(null); // Permet de tester la branche "alternative" (null check)

        PagedResponse<BookResponse> mockPage = mock(PagedResponse.class);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(mockBook));

        when(bookService.getAllBooks(anyInt(), anyInt(), anyString(), anyString(), any(), any())).thenReturn(mockPage);
        when(exportService.exportBooksToExcel(anyList())).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/api/export/excel/books"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.xlsx"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExportLoansPdfAsAdmin() throws Exception {
        // Utilisation de RETURNS_DEEP_STUBS pour chaîner les mocks sans erreurs de types
        LoanResponse mockLoan = mock(LoanResponse.class, Mockito.RETURNS_DEEP_STUBS);
        when(mockLoan.getId()).thenReturn(10L);
        when(mockLoan.getStatus()).thenReturn(null);
        when(mockLoan.getBook().getTitle()).thenReturn("Clean Code");
        when(mockLoan.getUser().getUsername()).thenReturn("yassine");

        PagedResponse<LoanResponse> mockPage = mock(PagedResponse.class);
        when(mockPage.getContent()).thenReturn(Collections.singletonList(mockLoan));

        when(loanService.getAllLoans(anyInt(), anyInt(), any(), any())).thenReturn(mockPage);
        when(exportService.exportLoansToPdf(anyList())).thenReturn(new byte[]{7, 8, 9});

        mockMvc.perform(get("/api/export/pdf/loans"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loans.pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserTriesToExport() throws Exception {
        // Un simple utilisateur (ROLE_USER) ne doit pas avoir accès aux exports
        mockMvc.perform(get("/api/export/pdf/books"))
                .andExpect(status().isForbidden());
    }
}