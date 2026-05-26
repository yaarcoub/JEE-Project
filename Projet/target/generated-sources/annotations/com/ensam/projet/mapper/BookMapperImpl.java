package com.ensam.projet.mapper;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.entity.Book;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-26T15:04:18+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21 (Oracle Corporation)"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public BookResponse toResponse(Book book) {
        if ( book == null ) {
            return null;
        }

        BookResponse bookResponse = new BookResponse();

        fillActiveLoans( book, bookResponse );

        return bookResponse;
    }

    @Override
    public Book toEntity(BookRequest request) {
        if ( request == null ) {
            return null;
        }

        Book book = new Book();

        return book;
    }
}
