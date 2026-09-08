package com.finsight.workflow.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String agentName;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String outputDetails;

    private ZonedDateTime executedAt = ZonedDateTime.now();

    public WorkflowStep() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOutputDetails() { return outputDetails; }
    public void setOutputDetails(String outputDetails) { this.outputDetails = outputDetails; }
    public ZonedDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(ZonedDateTime executedAt) { this.executedAt = executedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String workflowId;
        private String agentName;
        private String status;
        private String outputDetails;
        private ZonedDateTime executedAt = ZonedDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder workflowId(String workflowId) { this.workflowId = workflowId; return this; }
        public Builder agentName(String agentName) { this.agentName = agentName; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder outputDetails(String outputDetails) { this.outputDetails = outputDetails; return this; }
        public Builder executedAt(ZonedDateTime executedAt) { this.executedAt = executedAt; return this; }

        public WorkflowStep build() {
            WorkflowStep s = new WorkflowStep();
            s.setId(id);
            s.setWorkflowId(workflowId);
            s.setAgentName(agentName);
            s.setStatus(status);
            s.setOutputDetails(outputDetails);
            s.setExecutedAt(executedAt);
            return s;
        }
    }
}
