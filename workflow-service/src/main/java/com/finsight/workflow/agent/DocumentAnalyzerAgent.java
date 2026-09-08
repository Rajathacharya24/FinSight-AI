package com.finsight.workflow.agent;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DocumentAnalyzerAgent {

    public static class AnalysisResult {
        private boolean valid;
        private String details;
        private BigDecimal confidenceScore;

        public AnalysisResult() {}
        public AnalysisResult(boolean valid, String details, BigDecimal confidenceScore) {
            this.valid = valid;
            this.details = details;
            this.confidenceScore = confidenceScore;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public BigDecimal getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private boolean valid;
            private String details;
            private BigDecimal confidenceScore;
            public Builder valid(boolean valid) { this.valid = valid; return this; }
            public Builder details(String details) { this.details = details; return this; }
            public Builder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
            public AnalysisResult build() { return new AnalysisResult(valid, details, confidenceScore); }
        }
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
