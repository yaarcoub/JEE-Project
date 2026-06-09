package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.entity.Loan;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-10T00:20:40+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Red Hat, Inc.)"
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

        loanResponse.setId( loan.getId() );
        loanResponse.setLoanDate( loan.getLoanDate() );
        loanResponse.setExpectedReturnDate( loan.getExpectedReturnDate() );
        loanResponse.setActualReturnDate( loan.getActualReturnDate() );
        loanResponse.setStatus( loan.getStatus() );
        loanResponse.setBook( bookMapper.toResponse( loan.getBook() ) );
        loanResponse.setUser( userMapper.toResponse( loan.getUser() ) );
        loanResponse.setDetail( loanDetailMapper.toResponse( loan.getDetail() ) );

        return loanResponse;
    }
}
