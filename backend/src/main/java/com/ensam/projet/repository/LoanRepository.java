package com.ensam.projet.repository;

import com.ensam.projet.entity.Loan;
import com.ensam.projet.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    Page<Loan> findByUserId(Long userId, Pageable pageable);
    Page<Loan> findByBookId(Long bookId, Pageable pageable);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, LoanStatus status);
    long countByStatus(LoanStatus status);
    long countByBookId(Long bookId);
    @Query("select l from Loan l where (:status is null or l.status = :status) and (:userId is null or l.user.id = :userId)")
    List<Loan> searchByStatusAndUserId(@Param("status") LoanStatus status, @Param("userId") Long userId);
    long countByLoanDateBetween(LocalDate start, LocalDate end);
}
