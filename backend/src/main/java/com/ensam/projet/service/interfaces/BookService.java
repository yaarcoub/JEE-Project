package com.ensam.projet.service.interfaces;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;

import java.util.Set;

public interface BookService {
    PagedResponse<BookResponse> getAllBooks(int page, int size, String sort, String direction, String search, Long categoryId);
    BookResponse getBookById(Long id);
    BookResponse createBook(BookRequest request);
    BookResponse updateBook(Long id, BookRequest request);
    void deleteBook(Long id);
    BookResponse assignCategories(Long id, Set<Long> categoryIds);
    PagedResponse<LoanResponse> getBookLoans(Long id, int page, int size);
}
