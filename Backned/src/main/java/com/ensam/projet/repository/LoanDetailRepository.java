package com.ensam.projet.repository;

import com.ensam.projet.entity.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {
}
