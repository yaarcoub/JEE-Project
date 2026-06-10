package com.ensam.projet.controller;

import com.ensam.projet.dto.request.CategoryRequest;
import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.service.interfaces.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    "spring.datasource.url=jdbc:h2:mem:categoryctrltestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=",
    "spring.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryResponse categoryResponse;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Informatique");
        categoryResponse.setDescription("Livres sur le développement et la tech");

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Informatique");
        categoryRequest.setDescription("Livres sur le développement et la tech");
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.singletonList(categoryResponse));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Informatique"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetCategoryById() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Informatique"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldCreateCategoryAsManager() throws Exception {
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Informatique"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenCreateCategoryAsUser() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateCategoryAsAdmin() throws Exception {
        when(categoryService.updateCategory(anyLong(), any(CategoryRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Informatique"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteCategoryAsAdmin() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenDeleteCategoryAsManager() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isForbidden());
    }
}