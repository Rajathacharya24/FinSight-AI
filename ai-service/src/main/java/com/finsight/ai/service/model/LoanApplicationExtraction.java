package com.finsight.ai.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "loan_application_extraction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationExtraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    // ── Core extracted fields ──
    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "income")
    private String income;

    @Column(name = "address")
    private String address;

    @Column(name = "loan_amount")
    private String loanAmount;

    // ── Extended fields per Phase 5 spec ──
    @Column(name = "employment_status")
    private String employmentStatus;

    @Column(name = "existing_debts")
    private String existingDebts;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * AI confidence score [0, 100] — stored for audit and analytics.
     * Computed from retrieval quality, reranker scores, and extraction completeness.
     */
    @Column(name = "confidence_score")
    private Double confidenceScore;

    /**
     * Per-field confidence scores as a JSON string.
     * Example: {"applicantName":90,"income":75,"address":60,"loanAmount":85}
     */
    @Column(name = "field_confidence_scores", columnDefinition = "TEXT")
    private String fieldConfidenceScores;

    /** Wall-clock duration of the full RAG + LLM call in milliseconds (for analytics). */
    @Column(name = "extraction_duration_ms")
    private Long extractionDurationMs;

    /** Approximate LLM token usage (prompt + completion) for cost tracking. */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /** workflowId for lookup — a workflow always maps 1:1 to an extraction result. */
    @Column(name = "workflow_id")
    private Long workflowId;
}
