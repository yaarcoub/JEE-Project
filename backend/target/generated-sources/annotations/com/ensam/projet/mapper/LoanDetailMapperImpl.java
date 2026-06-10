package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanDetailResponse;
import com.ensam.projet.entity.LoanDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-10T09:15:11+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Red Hat, Inc.)"
)
@Component
public class LoanDetailMapperImpl implements LoanDetailMapper {

    @Override
    public LoanDetailResponse toResponse(LoanDetail detail) {
        if ( detail == null ) {
            return null;
        }

        LoanDetailResponse loanDetailResponse = new LoanDetailResponse();

        loanDetailResponse.setId( detail.getId() );
        loanDetailResponse.setNotes( detail.getNotes() );
        loanDetailResponse.setRenewalCount( detail.getRenewalCount() );
        loanDetailResponse.setReturnedBy( detail.getReturnedBy() );

        return loanDetailResponse;
    }
}
