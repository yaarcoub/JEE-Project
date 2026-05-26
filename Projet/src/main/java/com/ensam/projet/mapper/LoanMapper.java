package com.ensam.projet.mapper;

import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.entity.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BookMapper.class, UserMapper.class, LoanDetailMapper.class})
public interface LoanMapper {
    LoanResponse toResponse(Loan loan);
}
