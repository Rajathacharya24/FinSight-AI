package com.finsight.ai.service.repository;

import com.finsight.ai.service.model.LoanApplicationExtraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationExtractionRepository extends JpaRepository<LoanApplicationExtraction, Long> {

    Optional<LoanApplicationExtraction> findFirstByWorkflowIdOrderByCreatedAtDesc(Long workflowId);

    List<LoanApplicationExtraction> findByDocumentId(Long documentId);
}
