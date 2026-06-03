package com.ensam.projet.service.impl;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.exception.ResourceNotFoundException;
import com.ensam.projet.mapper.BookMapper;
import com.ensam.projet.mapper.LoanMapper;
import com.ensam.projet.repository.BookRepository;
import com.ensam.projet.repository.CategoryRepository;
import com.ensam.projet.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookRequest request;

    @BeforeEach
    void init() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Spring Boot in Action");
        book.setAuthor("Craig Walls");
        book.setIsbn("978-1617292545");
        book.setStock(5);
        request = new BookRequest();
        request.setTitle("Spring Boot in Action");
        request.setAuthor("Craig Walls");
        request.setIsbn("978-1617292545");
        request.setStock(5);
    }

    @Test
    void shouldReturnPagedBooks() {
        Page<Book> page = new PageImpl<>(Collections.singletonList(book));
        when(bookRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class))).thenReturn(page);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.getAllBooks(0, 10, "title", "asc", null, null);

        assertThat(response.getContent()).hasSize(1);
        verify(bookRepository, times(1)).findAll(org.mockito.ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class));
    }

    @Test
    void shouldReturnBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.getBookById(1L);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(2L));
    }

    @Test
    void shouldCreateBook() {
        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(bookMapper.toEntity(request)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.createBook(request);

        assertThat(response).isNotNull();
        verify(bookRepository).save(book);
    }

    @Test
    void shouldUpdateBook() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.updateBook(1L, request);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldDeleteBook() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        bookService.deleteBook(1L);
        verify(bookRepository).deleteById(1L);
    }
}
