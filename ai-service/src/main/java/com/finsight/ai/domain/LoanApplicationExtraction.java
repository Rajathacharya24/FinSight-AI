package com.finsight.ai.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "loan_application_extractions")
public class LoanApplicationExtraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String documentId;

    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;
    private String applicationDate;
    private String loanPurpose;
    private String employmentStatus;
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String rawExtractionJson;

    private ZonedDateTime createdAt = ZonedDateTime.now();

    public LoanApplicationExtraction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
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
    public String getRawExtractionJson() { return rawExtractionJson; }
    public void setRawExtractionJson(String rawExtractionJson) { this.rawExtractionJson = rawExtractionJson; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String documentId;
        private String borrowerName;
        private BigDecimal loanAmount;
        private BigDecimal annualIncome;
        private String applicationDate;
        private String loanPurpose;
        private String employmentStatus;
        private BigDecimal confidenceScore;
        private String rawExtractionJson;
        private ZonedDateTime createdAt = ZonedDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder borrowerName(String borrowerName) { this.borrowerName = borrowerName; return this; }
        public Builder loanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; return this; }
        public Builder annualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; return this; }
        public Builder applicationDate(String applicationDate) { this.applicationDate = applicationDate; return this; }
        public Builder loanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; return this; }
        public Builder employmentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; return this; }
        public Builder confidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Builder rawExtractionJson(String rawExtractionJson) { this.rawExtractionJson = rawExtractionJson; return this; }
        public Builder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }

        public LoanApplicationExtraction build() {
            LoanApplicationExtraction e = new LoanApplicationExtraction();
            e.setId(id);
            e.setDocumentId(documentId);
            e.setBorrowerName(borrowerName);
            e.setLoanAmount(loanAmount);
            e.setAnnualIncome(annualIncome);
            e.setApplicationDate(applicationDate);
            e.setLoanPurpose(loanPurpose);
            e.setEmploymentStatus(employmentStatus);
            e.setConfidenceScore(confidenceScore);
            e.setRawExtractionJson(rawExtractionJson);
            e.setCreatedAt(createdAt);
            return e;
        }
    }
}
