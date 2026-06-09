package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:mappertestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
@SpringBootTest
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void shouldMapCategoryWithNullBooksList() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Science Fiction");
        category.setBooks(null); // Cas critique pour ta condition java(...)

        CategoryResponse response = categoryMapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Science Fiction");
        assertThat(response.getBookCount()).isZero();
    }

    @Test
    void shouldMapCategoryAndCountBooks() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Fantasy");
        
        // Utilisation d'un Set (HashSet) au lieu d'une List
        java.util.Set<Book> books = new java.util.HashSet<>();
        
        // On donne des IDs différents pour s'assurer que le Set compte bien 2 éléments distincts
        Book book1 = new Book();
        book1.setId(1L);
        Book book2 = new Book();
        book2.setId(2L);
        
        books.add(book1);
        books.add(book2);
        category.setBooks(books);

        CategoryResponse response = categoryMapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getBookCount()).isEqualTo(2);
    }

    @Test
    void shouldReturnNullWhenCategoryIsNull() {
        assertThat(categoryMapper.toResponse(null)).isNull();
    }
}