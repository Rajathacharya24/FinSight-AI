package com.finsight.ai.dto;

import java.math.BigDecimal;

public class ExtractionDto {
    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;
    private String applicationDate;
    private String loanPurpose;
    private String employmentStatus;
    private BigDecimal confidenceScore;

    public ExtractionDto() {}

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public BigDecimal getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; }
    public String getApplicationDate() { return applicationDate; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }
    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String borrowerName;
        private BigDecimal loanAmount;
        private BigDecimal annualIncome;
        private String applicationDate;
        private String loanPurpose;
        private String employmentStatus;
        private BigDecimal confidenceScore;

        public Builder borrowerName(String borrowerName) { this.borrowerName = borrowerName; return this; }
        public Builder loanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; return this; }
        public Builder annualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; return this; }
        public Builder applicationDate(String applicationDate) { this.applicationDate = applicationDate; return this; }
        public Builder loanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; return this; }
        public Builder employmentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; return this; }
        public Builder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }

        public ExtractionDto build() {
            ExtractionDto e = new ExtractionDto();
            e.setBorrowerName(borrowerName);
            e.setLoanAmount(loanAmount);
            e.setAnnualIncome(annualIncome);
            e.setApplicationDate(applicationDate);
            e.setLoanPurpose(loanPurpose);
            e.setEmploymentStatus(employmentStatus);
            e.setConfidenceScore(confidenceScore);
            return e;
        }
    }
}
