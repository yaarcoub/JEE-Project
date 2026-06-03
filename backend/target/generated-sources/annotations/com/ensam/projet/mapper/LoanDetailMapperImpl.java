package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanDetailResponse;
import com.ensam.projet.entity.LoanDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-02T20:17:26+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
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
