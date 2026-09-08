package com.finsight.ai.event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class AIExtractionCompletedEvent {
    private String documentId;
    private String userId;
    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;
    private String applicationDate;
    private String loanPurpose;
    private String employmentStatus;
    private BigDecimal confidenceScore;
    private ZonedDateTime timestamp;
    private String correlationId;

    public AIExtractionCompletedEvent() {}

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
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
    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String documentId;
        private String userId;
        private String borrowerName;
        private BigDecimal loanAmount;
        private BigDecimal annualIncome;
        private String applicationDate;
        private String loanPurpose;
        private String employmentStatus;
        private BigDecimal confidenceScore;
        private ZonedDateTime timestamp;
        private String correlationId;

        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder borrowerName(String borrowerName) { this.borrowerName = borrowerName; return this; }
        public Builder loanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; return this; }
        public Builder annualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; return this; }
        public Builder applicationDate(String applicationDate) { this.applicationDate = applicationDate; return this; }
        public Builder loanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; return this; }
        public Builder employmentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; return this; }
        public Builder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

        public AIExtractionCompletedEvent build() {
            AIExtractionCompletedEvent e = new AIExtractionCompletedEvent();
            e.setDocumentId(documentId);
            e.setUserId(userId);
            e.setBorrowerName(borrowerName);
            e.setLoanAmount(loanAmount);
            e.setAnnualIncome(annualIncome);
            e.setApplicationDate(applicationDate);
            e.setLoanPurpose(loanPurpose);
            e.setEmploymentStatus(employmentStatus);
            e.setConfidenceScore(confidenceScore);
            e.setTimestamp(timestamp);
            e.setCorrelationId(correlationId);
            return e;
        }
    }
}
