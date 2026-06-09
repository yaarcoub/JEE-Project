package com.ensam.projet.repository;

import com.ensam.projet.entity.Book;
import com.ensam.projet.entity.Loan;
import com.ensam.projet.entity.LoanStatus;
import com.ensam.projet.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:loanreptestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User user;
    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password123");
        userRepository.save(user);

        book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("978-0000000000");
        book.setStock(5);
        bookRepository.save(book);

        loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setExpectedReturnDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loan);
    }

    @Test
    void shouldFindByUserId() {
        Page<Loan> result = loanRepository.findByUserId(user.getId(), PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldFindByBookId() {
        Page<Loan> result = loanRepository.findByBookId(book.getId(), PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBook().getId()).isEqualTo(book.getId());
    }

    @Test
    void shouldReturnTrueIfExistsByUserIdAndBookIdAndStatus() {
        boolean exists = loanRepository.existsByUserIdAndBookIdAndStatus(user.getId(), book.getId(), LoanStatus.ACTIVE);
        
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseIfNotExistsByUserIdAndBookIdAndStatus() {
        boolean exists = loanRepository.existsByUserIdAndBookIdAndStatus(user.getId(), book.getId(), LoanStatus.RETURNED);
        
        assertThat(exists).isFalse();
    }

    @Test
    void shouldCountByStatus() {
        long count = loanRepository.countByStatus(LoanStatus.ACTIVE);
        
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldCountByBookId() {
        long count = loanRepository.countByBookId(book.getId());
        
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldSearchByStatusAndUserId() {
        List<Loan> result = loanRepository.searchByStatusAndUserId(LoanStatus.ACTIVE, user.getId());
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void shouldSearchByStatusNullAndUserId() {
        List<Loan> result = loanRepository.searchByStatusAndUserId(null, user.getId());
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldCountByLoanDateBetween() {
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);
        
        long count = loanRepository.countByLoanDateBetween(start, end);
        
        assertThat(count).isEqualTo(1);
    }
}