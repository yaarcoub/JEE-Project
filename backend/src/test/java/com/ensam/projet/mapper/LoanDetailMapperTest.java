package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanDetailResponse;
import com.ensam.projet.entity.LoanDetail;
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
class LoanDetailMapperTest {

    @Autowired
    private LoanDetailMapper loanDetailMapper;

    @Test
    void shouldMapLoanDetailToResponse() {
        LoanDetail detail = new LoanDetail();
        detail.setId(1L);
        detail.setNotes("Livre en bon état");

        LoanDetailResponse response = loanDetailMapper.toResponse(detail);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNotes()).isEqualTo("Livre en bon état");
    }

    @Test
    void shouldReturnNullWhenLoanDetailIsNull() {
        assertThat(loanDetailMapper.toResponse(null)).isNull();
    }
}