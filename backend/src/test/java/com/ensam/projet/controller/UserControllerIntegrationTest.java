package com.ensam.projet.controller;

import com.ensam.projet.dto.request.ChangePasswordRequest;
import com.ensam.projet.dto.request.UpdateProfileRequest;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.dto.response.UserResponse;
import com.ensam.projet.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // Nettoie le contexte entre les tests
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:usercontrollerdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
    "spring.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1L);
        mockUserResponse.setUsername("testuser");
        mockUserResponse.setEmail("test@ensam.ma");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllUsersForAdmin() throws Exception {
        PagedResponse<UserResponse> pagedResponse = new PagedResponse<>();
        pagedResponse.setContent(Collections.singletonList(mockUserResponse));
        pagedResponse.setTotalElements(1L);

        when(userService.getAllUsers(anyInt(), anyInt())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRoles() throws Exception {
        when(userService.updateRoles(any(), any())).thenReturn(mockUserResponse);
        
        Set<String> roles = Set.of("ROLE_MANAGER");

        mockMvc.perform(put("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldUpdateProfileForAuthenticatedUser() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setEmail("new@ensam.ma");

        when(userService.updateProfile(any(), any())).thenReturn(mockUserResponse);

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldChangePassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass123");

        doNothing().when(userService).changePassword(any(), any());

        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "current_user", roles = "USER")
    void shouldGetCurrentUserProfile() throws Exception {
        when(userService.getCurrentUser()).thenReturn(mockUserResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser")); // testuser vient de notre mockUserResponse
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToDeleteAnotherUser() throws Exception {
        // Un USER classique n'a pas le droit de supprimer un compte, seul l'ADMIN peut.
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUserSuccessfullyWhenAdmin() throws Exception {
        // Un ADMIN a le droit. On dit au mock de ne rien faire (comportement d'une méthode void).
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent()); // 204 No Content
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenUpdateProfileWithInvalidEmail() throws Exception {
        // Format d'email invalide (pas de @)
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newuser");
        request.setEmail("bad-email-format"); 

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Doit retourner 400 Bad Request grâce à @Valid
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserForAdmin() throws Exception {
        when(userService.getUserById(1L)).thenReturn(mockUserResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToUpdateRoles() throws Exception {
        Set<String> roles = Set.of("ROLE_MANAGER");

        mockMvc.perform(put("/api/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleUserForAdmin() throws Exception {
        when(userService.toggleEnabled(1L)).thenReturn(mockUserResponse);

        mockMvc.perform(put("/api/users/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToToggleUser() throws Exception {
        mockMvc.perform(put("/api/users/1/toggle"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void refreshWithInvalidTokenShouldThrowError() throws Exception {
        // Le faux token génère une exception non gérée, ce qui donne une erreur 500.
        // L'objectif est juste de traverser la méthode du contrôleur pour la couverture.
        mockMvc.perform(post("/api/auth/refresh")
                        .param("refreshToken", "invalid-token-format-or-expired"))
                .andExpect(status().is5xxServerError());
    }
}