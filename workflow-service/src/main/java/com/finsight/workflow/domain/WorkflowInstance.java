package com.finsight.workflow.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_instances")
public class WorkflowInstance {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String documentId;

    @Column(nullable = false)
    private String userId;

    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;

    @Column(nullable = false)
    private String status;

    private String decision;
    
    @Column(columnDefinition = "TEXT")
    private String decisionReason;

    private ZonedDateTime startedAt = ZonedDateTime.now();
    private ZonedDateTime completedAt;
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @OneToMany(mappedBy = "workflowId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<WorkflowStep> steps = new ArrayList<>();

    public WorkflowInstance() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public ZonedDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; }
    public ZonedDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<WorkflowStep> getSteps() { return steps; }
    public void setSteps(List<WorkflowStep> steps) { this.steps = steps; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String documentId;
        private String userId;
        private String borrowerName;
        private BigDecimal loanAmount;
        private BigDecimal annualIncome;
        private String status;
        private String decision;
        private String decisionReason;
        private ZonedDateTime startedAt = ZonedDateTime.now();
        private ZonedDateTime completedAt;
        private ZonedDateTime updatedAt = ZonedDateTime.now();
        private List<WorkflowStep> steps = new ArrayList<>();

        public Builder id(String id) { this.id = id; return this; }
        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder borrowerName(String borrowerName) { this.borrowerName = borrowerName; return this; }
        public Builder loanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; return this; }
        public Builder annualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder decision(String decision) { this.decision = decision; return this; }
        public Builder decisionReason(String decisionReason) { this.decisionReason = decisionReason; return this; }
        public Builder startedAt(ZonedDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(ZonedDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder updatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder steps(List<WorkflowStep> steps) { this.steps = steps; return this; }

        public WorkflowInstance build() {
            WorkflowInstance w = new WorkflowInstance();
            w.setId(id);
            w.setDocumentId(documentId);
            w.setUserId(userId);
            w.setBorrowerName(borrowerName);
            w.setLoanAmount(loanAmount);
            w.setAnnualIncome(annualIncome);
            w.setStatus(status);
            w.setDecision(decision);
            w.setDecisionReason(decisionReason);
            w.setStartedAt(startedAt);
            w.setCompletedAt(completedAt);
            w.setUpdatedAt(updatedAt);
            w.setSteps(steps);
            return w;
        }
    }
}
