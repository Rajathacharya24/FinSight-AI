package com.finsight.workflow.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "workflow_audit_log")
public class WorkflowAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String performedBy = "SYSTEM";
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public WorkflowAuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String workflowId;
        private String action;
        private String description;
        private String performedBy = "SYSTEM";
        private ZonedDateTime timestamp = ZonedDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder workflowId(String workflowId) { this.workflowId = workflowId; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder performedBy(String performedBy) { this.performedBy = performedBy; return this; }
        public Builder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }

        public WorkflowAuditLog build() {
            WorkflowAuditLog a = new WorkflowAuditLog();
            a.setId(id);
            a.setWorkflowId(workflowId);
            a.setAction(action);
            a.setDescription(description);
            a.setPerformedBy(performedBy);
            a.setTimestamp(timestamp);
            return a;
        }
    }
}
