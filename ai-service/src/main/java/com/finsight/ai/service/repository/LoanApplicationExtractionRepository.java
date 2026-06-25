package com.finsight.ai.service.repository;

import com.finsight.ai.service.model.LoanApplicationExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApplicationExtractionRepository extends JpaRepository<LoanApplicationExtraction, Long> {
    // Additional query methods if needed
}
