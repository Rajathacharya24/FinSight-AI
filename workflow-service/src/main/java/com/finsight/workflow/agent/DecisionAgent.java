package com.finsight.workflow.agent;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DecisionAgent {

    @Data
    @Builder
    public static class DecisionResult {
        private String decision; // APPROVED, REJECTED, NEEDS_REVIEW
        private String reason;
    }

    public DecisionResult makeDecision(AIExtractionCompletedEvent event,
                                        DocumentAnalyzerAgent.AnalysisResult analysis,
                                        ComplianceAgent.ComplianceResult compliance) {
        
        if (!analysis.isValid()) {
            return DecisionResult.builder()
                    .decision("REJECTED")
                    .reason("Rejected by Document Analyzer Agent: " + analysis.getDetails())
                    .build();
        }

        if (analysis.getConfidenceScore().compareTo(new BigDecimal("0.70")) < 0) {
            return DecisionResult.builder()
                    .decision("NEEDS_REVIEW")
                    .reason("Flagged for Manual Review: Low AI extraction confidence score (" + 
                            analysis.getConfidenceScore().multiply(new BigDecimal("100")) + "%).")
                    .build();
        }

        if (!compliance.isPassed()) {
            return DecisionResult.builder()
                    .decision("REJECTED")
                    .reason("Rejected by Compliance Agent: DTI ratio (" + compliance.getDebtToIncomeRatio() + "%) exceeds maximum allowed threshold of 45%.")
                    .build();
        }

        if (event.getLoanAmount().compareTo(new BigDecimal("500000")) > 0) {
            return DecisionResult.builder()
                    .decision("NEEDS_REVIEW")
                    .reason("Flagged for Senior Underwriter Review: Loan amount exceeds automatic approval limit ($500,000).")
                    .build();
        }

        return DecisionResult.builder()
                .decision("APPROVED")
                .reason(String.format("Application Approved: Applicant '%s' meets all income, DTI (%.2f%%), and compliance underwriting standards.",
                        event.getBorrowerName(), compliance.getDebtToIncomeRatio().doubleValue()))
                .build();
    }
}
