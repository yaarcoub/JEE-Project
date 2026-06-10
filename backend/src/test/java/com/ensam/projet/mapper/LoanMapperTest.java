package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.entity.Loan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:mappertestdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk="
})
class LoanMapperTest {

    @Autowired
    private LoanMapper loanMapper;

    @Test
    void shouldMapLoanToResponse() {
        Loan loan = new Loan();
        loan.setId(5L);

        LoanResponse response = loanMapper.toResponse(loan);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    void shouldReturnNullWhenLoanIsNull() {
        assertThat(loanMapper.toResponse(null)).isNull();
    }
}