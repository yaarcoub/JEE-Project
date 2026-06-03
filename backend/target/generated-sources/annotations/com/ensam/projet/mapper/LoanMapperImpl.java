package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.entity.Loan;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-02T20:17:26+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class LoanMapperImpl implements LoanMapper {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoanDetailMapper loanDetailMapper;

    @Override
    public LoanResponse toResponse(Loan loan) {
        if ( loan == null ) {
            return null;
        }

        LoanResponse loanResponse = new LoanResponse();

        loanResponse.setActualReturnDate( loan.getActualReturnDate() );
        loanResponse.setBook( bookMapper.toResponse( loan.getBook() ) );
        loanResponse.setDetail( loanDetailMapper.toResponse( loan.getDetail() ) );
        loanResponse.setExpectedReturnDate( loan.getExpectedReturnDate() );
        loanResponse.setId( loan.getId() );
        loanResponse.setLoanDate( loan.getLoanDate() );
        loanResponse.setStatus( loan.getStatus() );
        loanResponse.setUser( userMapper.toResponse( loan.getUser() ) );

        return loanResponse;
    }
}
