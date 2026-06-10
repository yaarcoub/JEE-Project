package com.ensam.projet.controller;

import com.ensam.projet.dto.request.LoanRequest;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.service.interfaces.LoanService;
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

import java.time.LocalDate;
import java.util.Collections;

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
    "spring.datasource.url=jdbc:h2:mem:loanctrltestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
    "spring.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    private LoanResponse loanResponse;
    private LoanRequest loanRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse();
        userResponse.setUsername("testuser");

        loanResponse = new LoanResponse();
        loanResponse.setId(1L);
        loanResponse.setUser(userResponse);

        // Ajout des champs pour passer la validation @Valid
        loanRequest = new LoanRequest();
        loanRequest.setBookId(1L);
        loanRequest.setExpectedReturnDate(LocalDate.now().plusDays(14));
        loanRequest.setCondition("GOOD");
        loanRequest.setNotes("Test notes");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllLoansAsAdmin() throws Exception {
        PagedResponse<LoanResponse> pagedResponse = new PagedResponse<>(
                Collections.singletonList(loanResponse), 0, 1, 1L, 10, true
        );
        when(loanService.getAllLoans(anyInt(), anyInt(), any(), any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/loans")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenGetAllLoansAsUser() throws Exception {
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldGetLoanWhenOwner() throws Exception {
        when(loanService.getLoanById(1L)).thenReturn(loanResponse);

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = "USER")
    void shouldReturn403WhenGetLoanAsOtherUser() throws Exception {
        when(loanService.getLoanById(1L)).thenReturn(loanResponse);

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldGetLoanWhenManager() throws Exception {
        when(loanService.getLoanById(1L)).thenReturn(loanResponse);

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldCreateLoan() throws Exception {
        when(loanService.createLoan(any(LoanRequest.class))).thenReturn(loanResponse);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateLoan() throws Exception {
        when(loanService.updateLoan(anyLong(), any(LoanRequest.class))).thenReturn(loanResponse);

        mockMvc.perform(put("/api/loans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldReturnLoan() throws Exception {
        when(loanService.returnLoan(anyLong(), anyString())).thenReturn(loanResponse);

        mockMvc.perform(put("/api/loans/1/return")
                        .param("returnedBy", "manager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteLoanWhenAdmin() throws Exception {
        doNothing().when(loanService).deleteLoan(1L);

        mockMvc.perform(delete("/api/loans/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenDeleteLoanAsUser() throws Exception {
        mockMvc.perform(delete("/api/loans/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetMyLoans() throws Exception {
        PagedResponse<LoanResponse> pagedResponse = new PagedResponse<>(
                Collections.singletonList(loanResponse), 0, 1, 1L, 10, true
        );
        when(loanService.getMyLoans(anyInt(), anyInt())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/loans/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUpdateLoanAsUser() throws Exception {
        mockMvc.perform(put("/api/loans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenReturnLoanAsUser() throws Exception {
        mockMvc.perform(put("/api/loans/1/return")
                        .param("returnedBy", "user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenDeleteLoanAsManager() throws Exception {
        mockMvc.perform(delete("/api/loans/1"))
                .andExpect(status().isForbidden());
    }
}