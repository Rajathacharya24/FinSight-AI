package com.finsight.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResponse {

    private Long documentId;
    private Long workflowId;

    // ── Extracted fields ──
    private String applicantName;
    private String income;
    private String address;
    private String loanAmount;
    private String employmentStatus;
    private String existingDebts;

    private String status;

    /**
     * Aggregate confidence score in the range [0, 100].
     * Average of all per-field scores. Use {@link #fieldConfidenceScores} for per-field granularity.
     */
    private double confidenceScore;

    /**
     * Per-field confidence scores map: fieldName → [0, 100].
     * Frontend uses this to render confidence badges per extracted field.
     * Example: {"applicantName": 90, "income": 70, "address": 55, ...}
     */
    private Map<String, Integer> fieldConfidenceScores;

    /** Wall-clock extraction duration in milliseconds (for analytics). */
    private Long extractionDurationMs;
}
