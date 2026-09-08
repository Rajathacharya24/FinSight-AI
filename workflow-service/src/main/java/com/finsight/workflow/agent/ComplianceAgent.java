package com.finsight.workflow.agent;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ComplianceAgent {

    @Data
    @Builder
    public static class ComplianceResult {
        private boolean passed;
        private BigDecimal debtToIncomeRatio;
        private String details;
    }

    public ComplianceResult checkCompliance(AIExtractionCompletedEvent event) {
        BigDecimal income = event.getAnnualIncome();
        BigDecimal loanAmount = event.getLoanAmount();

        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return ComplianceResult.builder()
                    .passed(false)
                    .debtToIncomeRatio(BigDecimal.ZERO)
                    .details("Compliance Failed: Annual income is zero or unverified.")
                    .build();
        }

        // Estimate annual debt obligations (assuming 15% annual debt payment / loan amount ratio)
        BigDecimal estimatedAnnualPayment = loanAmount.multiply(new BigDecimal("0.15"));
        BigDecimal dti = estimatedAnnualPayment.divide(income, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

        boolean passesDti = dti.compareTo(new BigDecimal("45.00")) <= 0; // Max DTI 45%

        String details = String.format("Compliance Check Completed: Calculated DTI ratio is %.2f%% (Threshold: <= 45.00%%). Employment: '%s'. Status: %s",
                dti.doubleValue(),
                event.getEmploymentStatus() != null ? event.getEmploymentStatus() : "Unspecified",
                passesDti ? "PASSED" : "FAILED_HIGH_RISK");

        return ComplianceResult.builder()
                .passed(passesDti)
                .debtToIncomeRatio(dti)
                .details(details)
                .build();
    }
}
