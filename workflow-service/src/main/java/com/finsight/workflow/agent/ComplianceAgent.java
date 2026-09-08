package com.finsight.workflow.agent;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ComplianceAgent {

    public static class ComplianceResult {
        private boolean passed;
        private BigDecimal debtToIncomeRatio;
        private String details;

        public ComplianceResult() {}
        public ComplianceResult(boolean passed, BigDecimal debtToIncomeRatio, String details) {
            this.passed = passed;
            this.debtToIncomeRatio = debtToIncomeRatio;
            this.details = details;
        }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public BigDecimal getDebtToIncomeRatio() { return debtToIncomeRatio; }
        public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private boolean passed;
            private BigDecimal debtToIncomeRatio;
            private String details;
            public Builder passed(boolean passed) { this.passed = passed; return this; }
            public Builder debtToIncomeRatio(BigDecimal debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; return this; }
            public Builder details(String details) { this.details = details; return this; }
            public ComplianceResult build() { return new ComplianceResult(passed, debtToIncomeRatio, details); }
        }
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

        BigDecimal estimatedAnnualPayment = loanAmount.multiply(new BigDecimal("0.15"));
        BigDecimal dti = estimatedAnnualPayment.divide(income, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

        boolean passesDti = dti.compareTo(new BigDecimal("45.00")) <= 0;

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
