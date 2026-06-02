package com.ensam.projet.service.interfaces;

import com.ensam.projet.dto.request.LoanRequest;
import com.ensam.projet.dto.response.LoanResponse;
import com.ensam.projet.dto.response.PagedResponse;
import com.ensam.projet.entity.LoanStatus;

public interface LoanService {
    PagedResponse<LoanResponse> getAllLoans(int page, int size, LoanStatus status, Long userId);
    LoanResponse getLoanById(Long id);
    LoanResponse createLoan(LoanRequest request);
    LoanResponse updateLoan(Long id, LoanRequest request);
    LoanResponse returnLoan(Long id, String returnedBy);
    void deleteLoan(Long id);
    PagedResponse<LoanResponse> getMyLoans(int page, int size);
}
