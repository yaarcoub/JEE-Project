package com.ensam.projet.mapper;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.dto.response.CategoryResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Category;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-05T09:16:26+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Red Hat, Inc.)"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public BookResponse toResponse(Book book) {
        if ( book == null ) {
            return null;
        }

        BookResponse bookResponse = new BookResponse();

        bookResponse.setId( book.getId() );
        bookResponse.setTitle( book.getTitle() );
        bookResponse.setAuthor( book.getAuthor() );
        bookResponse.setIsbn( book.getIsbn() );
        bookResponse.setDescription( book.getDescription() );
        bookResponse.setStock( book.getStock() );
        bookResponse.setPublishedDate( book.getPublishedDate() );
        bookResponse.setCreatedAt( book.getCreatedAt() );
        bookResponse.setUpdatedAt( book.getUpdatedAt() );
        bookResponse.setCategories( categorySetToCategoryResponseSet( book.getCategories() ) );

        fillActiveLoans( book, bookResponse );

        return bookResponse;
    }

    @Override
    public Book toEntity(BookRequest request) {
        if ( request == null ) {
            return null;
        }

        Book book = new Book();

        book.setTitle( request.getTitle() );
        book.setAuthor( request.getAuthor() );
        book.setIsbn( request.getIsbn() );
        book.setDescription( request.getDescription() );
        book.setStock( request.getStock() );
        book.setPublishedDate( request.getPublishedDate() );

        return book;
    }

    protected Set<CategoryResponse> categorySetToCategoryResponseSet(Set<Category> set) {
        if ( set == null ) {
            return null;
        }

        Set<CategoryResponse> set1 = new LinkedHashSet<CategoryResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Category category : set ) {
            set1.add( categoryMapper.toResponse( category ) );
        }

        return set1;
    }
}
