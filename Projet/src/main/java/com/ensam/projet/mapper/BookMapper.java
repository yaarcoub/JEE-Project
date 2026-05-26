package com.ensam.projet.mapper;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.LoanStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface BookMapper {

    BookResponse toResponse(Book book);


    Book toEntity(BookRequest request);

    @AfterMapping
    default void fillActiveLoans(Book book, @MappingTarget BookResponse response) {
        if (book == null || book.getLoans() == null) {
            response.setActiveLoansCount(0);
            return;
        }

        long count = book.getLoans().stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .count();

        response.setActiveLoansCount(count);
    }
}