package com.ensam.projet.service.impl;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Category;
import com.ensam.projet.exception.BadRequestException;
import com.ensam.projet.exception.ResourceNotFoundException;
import com.ensam.projet.mapper.BookMapper;
import com.ensam.projet.mapper.LoanMapper;
import com.ensam.projet.repository.BookRepository;
import com.ensam.projet.repository.BookSpecification;
import com.ensam.projet.repository.CategoryRepository;
import com.ensam.projet.repository.LoanRepository;
import com.ensam.projet.service.NotificationService;
import com.ensam.projet.service.interfaces.BookService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final NotificationService notificationService;

    @Override
    public PagedResponse<BookResponse> getAllBooks(int page, int size, String sort, String direction, String search, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sort));
        Specification<Book> spec = Specification.where(BookSpecification.hasKeyword(search))
                .and(BookSpecification.hasCategory(categoryId));
        Page<Book> books = bookRepository.findAll(spec, pageable);
        return new PagedResponse<>(books.map(bookMapper::toResponse).getContent(), books.getNumber(), books.getTotalPages(), books.getTotalElements(), books.getSize(), books.isLast());
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));
        return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse createBook(BookRequest request) {
        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("ISBN déjà utilisé");
        }
        Book book = bookMapper.toEntity(request);
        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                categories.add(categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable")));
            }
            book.setCategories(categories);
        }
        Book savedBook = bookRepository.save(book);
        notificationService.sendNotification("Nouveau livre", "Le livre '" + savedBook.getTitle() + "' a été ajouté au catalogue.", "INFO");
        return bookMapper.toResponse(savedBook);
    }

    @Override
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("ISBN déjà utilisé");
        }
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setStock(request.getStock());
        book.setPublishedDate(request.getPublishedDate());
        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : request.getCategoryIds()) {
                categories.add(categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable")));
            }
            book.setCategories(categories);
        }
        Book savedBook = bookRepository.save(book);
        notificationService.sendNotification("Livre mis à jour", "Les informations du livre '" + savedBook.getTitle() + "' ont été mises à jour.", "INFO");
        return bookMapper.toResponse(savedBook);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre introuvable");
        }
        bookRepository.deleteById(id);
        notificationService.sendNotification("Livre supprimé", "Un livre a été retiré du catalogue.", "WARNING");
    }

    @Override
    public BookResponse assignCategories(Long id, Set<Long> categoryIds) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable"));
        Set<Category> categories = new HashSet<>();
        for (Long categoryId : categoryIds) {
            categories.add(categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable")));
        }
        book.setCategories(categories);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public PagedResponse<LoanResponse> getBookLoans(Long id, int page, int size) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre introuvable");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "loanDate"));
        var loanPage = loanRepository.findByBookId(id, pageable);
        return new PagedResponse<>(loanPage.map(loanMapper::toResponse).getContent(),
                loanPage.getNumber(), loanPage.getTotalPages(), loanPage.getTotalElements(), loanPage.getSize(), loanPage.isLast());
    }
}
