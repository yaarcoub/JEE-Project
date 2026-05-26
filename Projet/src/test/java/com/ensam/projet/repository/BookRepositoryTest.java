package com.ensam.projet.repository;

import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindBookByIsbn() {
        Book book = new Book();
        book.setTitle("Test book");
        book.setAuthor("Author");
        book.setIsbn("978-0000000000");
        book.setStock(1);
        book.setPublishedDate(LocalDate.now());
        bookRepository.save(book);

        assertThat(bookRepository.existsByIsbn("978-0000000000")).isTrue();
    }

    @Test
    void shouldFilterBooksByCategory() {
        Category category = new Category();
        category.setName("Test category");
        categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Test book");
        book.setAuthor("Author");
        book.setIsbn("978-1111111111");
        book.setStock(2);
        book.setPublishedDate(LocalDate.now());
        book.getCategories().add(category);
        bookRepository.save(book);

        Page<Book> page = bookRepository.findAll(BookSpecification.hasCategory(category.getId()), PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }
}
