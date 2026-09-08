package com.finsight.notification.event;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class WorkflowCompletedEvent {
    private String workflowId;
    private String documentId;
    private String userId;
    private String borrowerName;
    private BigDecimal loanAmount;
    private String decision;
    private String decisionReason;
    private ZonedDateTime timestamp;
    private String correlationId;

    public WorkflowCompletedEvent() {}

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String workflowId;
        private String documentId;
        private String userId;
        private String borrowerName;
        private BigDecimal loanAmount;
        private String decision;
        private String decisionReason;
        private ZonedDateTime timestamp;
        private String correlationId;

        public Builder workflowId(String workflowId) { this.workflowId = workflowId; return this; }
        public Builder documentId(String documentId) { this.documentId = documentId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder borrowerName(String borrowerName) { this.borrowerName = borrowerName; return this; }
        public Builder loanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; return this; }
        public Builder decision(String decision) { this.decision = decision; return this; }
        public Builder decisionReason(String decisionReason) { this.decisionReason = decisionReason; return this; }
        public Builder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

        public WorkflowCompletedEvent build() {
            WorkflowCompletedEvent e = new WorkflowCompletedEvent();
            e.setWorkflowId(workflowId);
            e.setDocumentId(documentId);
            e.setUserId(userId);
            e.setBorrowerName(borrowerName);
            e.setLoanAmount(loanAmount);
            e.setDecision(decision);
            e.setDecisionReason(decisionReason);
            e.setTimestamp(timestamp);
            e.setCorrelationId(correlationId);
            return e;
        }
    }
}
