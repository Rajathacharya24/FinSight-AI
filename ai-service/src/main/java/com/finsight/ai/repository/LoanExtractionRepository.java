package com.finsight.ai.repository;

import com.finsight.ai.domain.LoanApplicationExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoanExtractionRepository extends JpaRepository<LoanApplicationExtraction, Long> {
    Optional<LoanApplicationExtraction> findByDocumentId(String documentId);
}
