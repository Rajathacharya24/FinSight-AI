package com.finsight.workflowservice.dto;

import com.finsight.workflowservice.model.AgentType;
import com.finsight.workflowservice.model.AuditStatus;
import com.finsight.workflowservice.model.WorkflowStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntryDto {

    private Long id;
    private WorkflowStep step;
    private AgentType agent;
    private String action;
    private AuditStatus status;
    private String inputSummary;
    private String outputSummary;
    private Instant timestamp;
}
