package com.ensam.projet.controller;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.service.interfaces.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:bookctrltestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
    "spring.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookResponse bookResponse;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Spring Boot in Action");
        bookResponse.setAuthor("Craig Walls");
        bookResponse.setIsbn("978-1617292545");
        bookResponse.setStock(5);

        bookRequest = new BookRequest();
        bookRequest.setTitle("Spring Boot in Action");
        bookRequest.setAuthor("Craig Walls");
        bookRequest.setIsbn("978-1617292545");
        bookRequest.setStock(5);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllBooks() throws Exception {
        PagedResponse<BookResponse> pagedResponse = new PagedResponse<>(
                Collections.singletonList(bookResponse), 0, 1, 1L, 10, true
        );
        when(bookService.getAllBooks(anyInt(), anyInt(), anyString(), anyString(), any(), any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot in Action"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetBookById() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookResponse);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spring Boot in Action"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateBookWhenAdmin() throws Exception {
        when(bookService.createBook(any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Spring Boot in Action"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenCreateBookAsUser() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateBookWhenAdmin() throws Exception {
        when(bookService.updateBook(anyLong(), any(BookRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spring Boot in Action"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBookWhenAdmin() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldAssignCategories() throws Exception {
        Set<Long> categoryIds = Set.of(1L, 2L);
        when(bookService.assignCategories(anyLong(), any())).thenReturn(bookResponse);

        mockMvc.perform(post("/api/books/1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spring Boot in Action"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldGetBookLoans() throws Exception {
        PagedResponse<com.ensam.projet.dto.response.LoanResponse> pagedResponse = new PagedResponse<>(
                Collections.emptyList(), 0, 1, 0L, 10, true
        );
        when(bookService.getBookLoans(anyLong(), anyInt(), anyInt())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/books/1/loans")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenGetBookLoansAsUser() throws Exception {
        mockMvc.perform(get("/api/books/1/loans"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenDeleteBookAsManager() throws Exception {
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isForbidden());
    }
}