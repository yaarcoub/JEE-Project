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
    date = "2026-06-02T20:17:26+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
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

        bookResponse.setAuthor( book.getAuthor() );
        bookResponse.setCategories( categorySetToCategoryResponseSet( book.getCategories() ) );
        bookResponse.setCreatedAt( book.getCreatedAt() );
        bookResponse.setDescription( book.getDescription() );
        bookResponse.setId( book.getId() );
        bookResponse.setIsbn( book.getIsbn() );
        bookResponse.setPublishedDate( book.getPublishedDate() );
        bookResponse.setStock( book.getStock() );
        bookResponse.setTitle( book.getTitle() );
        bookResponse.setUpdatedAt( book.getUpdatedAt() );

        fillActiveLoans( book, bookResponse );

        return bookResponse;
    }

    @Override
    public Book toEntity(BookRequest request) {
        if ( request == null ) {
            return null;
        }

        Book book = new Book();

        book.setAuthor( request.getAuthor() );
        book.setDescription( request.getDescription() );
        book.setIsbn( request.getIsbn() );
        book.setPublishedDate( request.getPublishedDate() );
        book.setStock( request.getStock() );
        book.setTitle( request.getTitle() );

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
