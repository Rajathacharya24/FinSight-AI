package com.finsight.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResponse {
    private Long documentId;
    private String applicantName;
    private String income;
    private String address;
    private String loanAmount;
    private String status;

    /**
     * Confidence score in the range [0, 100].
     * Computed from retrieval quality (number of chunks found) and parse success.
     * 100 = all fields extracted, multiple relevant chunks retrieved.
     * Lower values indicate partial extraction or thin RAG context.
     */
    private double confidenceScore;
}
