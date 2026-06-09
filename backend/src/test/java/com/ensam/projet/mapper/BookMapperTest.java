package com.ensam.projet.mapper;

import com.ensam.projet.dto.request.BookRequest;
import com.ensam.projet.dto.response.BookResponse;
import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Loan;
import com.ensam.projet.entity.LoanStatus;
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
class BookMapperTest {

    @Autowired
    private BookMapper bookMapper;

    @Test
    void shouldMapBookToResponseWithNullLoans() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Dune");
        book.setLoans(null); // Déclenche le IF dans @AfterMapping

        BookResponse response = bookMapper.toResponse(book);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Dune");
        assertThat(response.getActiveLoansCount()).isZero();
    }

    @Test
    void shouldMapBookToResponseAndCountActiveLoans() {
        Book book = new Book();
        book.setId(2L);
        book.setTitle("1984");

        Loan activeLoan = new Loan();
        activeLoan.setStatus(LoanStatus.ACTIVE);

        Loan returnedLoan = new Loan();
        returnedLoan.setStatus(LoanStatus.RETURNED);

        List<Loan> loans = new ArrayList<>();
        loans.add(activeLoan);
        loans.add(returnedLoan);
        book.setLoans(loans);

        BookResponse response = bookMapper.toResponse(book);

        assertThat(response).isNotNull();
        assertThat(response.getActiveLoansCount()).isEqualTo(1); // Seul le ACTIVE doit être compté
    }

    @Test
    void shouldMapBookRequestToEntity() {
        BookRequest request = new BookRequest();
        request.setTitle("The Hobbit");
        request.setAuthor("J.R.R. Tolkien");

        Book book = bookMapper.toEntity(request);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("The Hobbit");
        assertThat(book.getAuthor()).isEqualTo("J.R.R. Tolkien");
    }

    @Test
    void shouldReturnNullWhenBookIsNull() {
        assertThat(bookMapper.toResponse(null)).isNull();
        assertThat(bookMapper.toEntity(null)).isNull();
    }
}