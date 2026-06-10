package com.ensam.projet.service.impl;

import com.ensam.projet.service.NotificationService;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.ensam.projet.exception.BadRequestException;
import com.ensam.projet.entity.Category;
import com.ensam.projet.dto.response.LoanResponse;
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
    @Mock
    private NotificationService notificationService;

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
    void shouldReturnPagedBooksWithCorrectPaginationAndSorting() {
        Page<Book> page = new PageImpl<>(Collections.singletonList(book));
        
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<Book>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        when(bookRepository.findAll(specCaptor.capture(), pageableCaptor.capture())).thenReturn(page);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.getAllBooks(1, 5, "title", "desc", "Spring", 2L);

        assertThat(response.getContent()).hasSize(1);
        
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(1);
        assertThat(capturedPageable.getPageSize()).isEqualTo(5);
        
        Sort.Order sortOrder = capturedPageable.getSort().getOrderFor("title");
        assertThat(sortOrder).isNotNull();
        assertThat(sortOrder.getDirection()).isEqualTo(Sort.Direction.DESC);
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

    @Test
    void shouldThrowWhenCreateBookIsbnExists() {
        request.setIsbn("12345");
        when(bookRepository.existsByIsbn("12345")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> bookService.createBook(request));
    }

    @Test
    void shouldCreateBookWithCategories() {
        request.setCategoryIds(Collections.singleton(1L));
        Category category = new Category();
        category.setId(1L);

        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(bookMapper.toEntity(request)).thenReturn(book);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.createBook(request);

        assertThat(response).isNotNull();
        assertThat(book.getCategories()).hasSize(1);
    }

    @Test
    void shouldThrowWhenCreateBookCategoryNotFound() {
        request.setCategoryIds(Collections.singleton(1L));
        when(bookRepository.existsByIsbn(request.getIsbn())).thenReturn(false);
        when(bookMapper.toEntity(request)).thenReturn(book);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(request));
    }

    @Test
    void shouldThrowWhenUpdateBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(1L, request));
    }

    @Test
    void shouldThrowWhenUpdateBookIsbnExists() {
        request.setIsbn("new-isbn");
        book.setIsbn("old-isbn");
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbn("new-isbn")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> bookService.updateBook(1L, request));
    }

    @Test
    void shouldUpdateBookWithCategories() {
        request.setCategoryIds(Collections.singleton(1L));
        Category category = new Category();
        category.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.updateBook(1L, request);

        assertThat(response).isNotNull();
        assertThat(book.getCategories()).hasSize(1);
    }

    @Test
    void shouldThrowWhenUpdateBookCategoryNotFound() {
        request.setCategoryIds(Collections.singleton(1L));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(1L, request));
    }

    @Test
    void shouldThrowWhenDeleteBookNotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(1L));
    }

    @Test
    void shouldAssignCategories() {
        Category category = new Category();
        category.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponse(book)).thenReturn(new BookResponse());

        var response = bookService.assignCategories(1L, Collections.singleton(1L));

        assertThat(response).isNotNull();
        assertThat(book.getCategories()).hasSize(1);
    }

    @Test
    void shouldThrowWhenAssignCategoriesBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.assignCategories(1L, Collections.singleton(1L)));
    }

    @Test
    void shouldThrowWhenAssignCategoriesCategoryNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.assignCategories(1L, Collections.singleton(1L)));
    }

    @Test
    void shouldGetBookLoans() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        Page<com.ensam.projet.entity.Loan> loanPage = new PageImpl<>(Collections.singletonList(new com.ensam.projet.entity.Loan()));
        when(loanRepository.findByBookId(eq(1L), any(Pageable.class))).thenReturn(loanPage);
        when(loanMapper.toResponse(any())).thenReturn(new LoanResponse());

        var response = bookService.getBookLoans(1L, 0, 10);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void shouldThrowWhenGetBookLoansBookNotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookLoans(1L, 0, 10));
    }
}