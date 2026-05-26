package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.entity.Loan;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-26T15:04:18+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21 (Oracle Corporation)"
)
@Component
public class LoanMapperImpl implements LoanMapper {

    @Override
    public LoanResponse toResponse(Loan loan) {
        if ( loan == null ) {
            return null;
        }

        LoanResponse loanResponse = new LoanResponse();

        return loanResponse;
    }
}
