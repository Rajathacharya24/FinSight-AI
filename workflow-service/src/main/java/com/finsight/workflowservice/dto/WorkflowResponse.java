package com.finsight.workflowservice.dto;

import com.finsight.workflowservice.model.WorkflowStatus;
import com.finsight.workflowservice.model.WorkflowStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {

    private Long workflowId;
    private Long documentId;
    private WorkflowStatus status;
    private WorkflowStep currentStep;
    private ExtractionData extraction;
    private ComplianceResult compliance;
    private DecisionResult decision;
    private List<AuditLogEntryDto> auditLog;
    private Instant createdAt;
    private Instant updatedAt;
}
