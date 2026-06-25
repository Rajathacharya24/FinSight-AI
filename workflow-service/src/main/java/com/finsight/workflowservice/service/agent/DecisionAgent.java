package com.finsight.workflowservice.service.agent;

import com.finsight.workflowservice.dto.ComplianceResult;
import com.finsight.workflowservice.dto.DecisionResult;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.model.AgentType;
import com.finsight.workflowservice.model.WorkflowStep;
import com.finsight.workflowservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecisionAgent {

    private final AuditLogService auditLogService;

    public DecisionResult recommend(Long workflowId, ExtractionData extraction, ComplianceResult compliance) {
        auditLogService.logStarted(workflowId, WorkflowStep.RECOMMEND, AgentType.DECISION_AGENT,
                "generate_recommendation", compliance);

        DecisionResult result;
        if (!compliance.isCompliant()) {
            result = DecisionResult.builder()
                    .recommendation("REJECT")
                    .rationale("Application rejected due to compliance violations: "
                            + String.join("; ", compliance.getViolations()))
                    .confidenceScore(0.95)
                    .build();
        } else if (!compliance.getWarnings().isEmpty()) {
            result = DecisionResult.builder()
                    .recommendation("REVIEW")
                    .rationale("Manual review recommended. Warnings: "
                            + String.join("; ", compliance.getWarnings()))
                    .confidenceScore(0.75)
                    .build();
        } else {
            result = DecisionResult.builder()
                    .recommendation("APPROVE")
                    .rationale("Application approved for "
                            + safe(extraction.getLoanAmount())
                            + " loan for applicant "
                            + safe(extraction.getApplicantName()))
                    .confidenceScore(0.90)
                    .build();
        }

        auditLogService.logCompleted(workflowId, WorkflowStep.RECOMMEND, AgentType.DECISION_AGENT,
                "generate_recommendation", result);
        return result;
    }

    private String safe(String value) {
        return value != null ? value : "unknown";
    }
}
