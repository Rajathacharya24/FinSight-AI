package com.finsight.workflow.agent;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DocumentAnalyzerAgent {

    @Data
    @Builder
    public static class AnalysisResult {
        private boolean valid;
        private String details;
        private BigDecimal confidenceScore;
    }

    public AnalysisResult analyze(AIExtractionCompletedEvent event) {
        if (event.getBorrowerName() == null || event.getBorrowerName().isBlank()) {
            return AnalysisResult.builder()
                    .valid(false)
                    .details("Document Analysis Failed: Missing borrower name.")
                    .confidenceScore(BigDecimal.ZERO)
                    .build();
        }

        if (event.getLoanAmount() == null || event.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return AnalysisResult.builder()
                    .valid(false)
                    .details("Document Analysis Failed: Invalid loan amount requested.")
                    .confidenceScore(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal confidence = event.getConfidenceScore() != null ? event.getConfidenceScore() : new BigDecimal("0.90");

        return AnalysisResult.builder()
                .valid(true)
                .details(String.format("Document Analysis Completed: Validated applicant '%s', requested loan amount $%.2f. AI Confidence: %.2f%%",
                        event.getBorrowerName(), event.getLoanAmount(), confidence.doubleValue() * 100))
                .confidenceScore(confidence)
                .build();
    }
}
