package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanDetailResponse;
import com.ensam.projet.entity.LoanDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanDetailMapper {
    LoanDetailResponse toResponse(LoanDetail detail);
}
