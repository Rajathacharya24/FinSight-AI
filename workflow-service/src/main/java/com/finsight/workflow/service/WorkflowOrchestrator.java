package com.finsight.workflow.service;

import com.finsight.workflow.agent.ComplianceAgent;
import com.finsight.workflow.agent.DecisionAgent;
import com.finsight.workflow.agent.DocumentAnalyzerAgent;
import com.finsight.workflow.domain.WorkflowAuditLog;
import com.finsight.workflow.domain.WorkflowInstance;
import com.finsight.workflow.domain.WorkflowStep;
import com.finsight.workflow.event.AIExtractionCompletedEvent;
import com.finsight.workflow.event.WorkflowCompletedEvent;
import com.finsight.workflow.repository.WorkflowAuditLogRepository;
import com.finsight.workflow.repository.WorkflowInstanceRepository;
import com.finsight.workflow.repository.WorkflowStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class WorkflowOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestrator.class);
    private final DocumentAnalyzerAgent documentAnalyzerAgent;
    private final ComplianceAgent complianceAgent;
    private final DecisionAgent decisionAgent;

    private final WorkflowInstanceRepository instanceRepository;
    private final WorkflowStepRepository stepRepository;
    private final WorkflowAuditLogRepository auditLogRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_WORKFLOW_COMPLETED = "workflow-completed-topic";

    public WorkflowOrchestrator(DocumentAnalyzerAgent documentAnalyzerAgent,
                                ComplianceAgent complianceAgent,
                                DecisionAgent decisionAgent,
                                WorkflowInstanceRepository instanceRepository,
                                WorkflowStepRepository stepRepository,
                                WorkflowAuditLogRepository auditLogRepository,
                                KafkaTemplate<String, Object> kafkaTemplate) {
        this.documentAnalyzerAgent = documentAnalyzerAgent;
        this.complianceAgent = complianceAgent;
        this.decisionAgent = decisionAgent;
        this.instanceRepository = instanceRepository;
        this.stepRepository = stepRepository;
        this.auditLogRepository = auditLogRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public WorkflowInstance executeWorkflow(AIExtractionCompletedEvent event) {
        String workflowId = UUID.randomUUID().toString();

        WorkflowInstance instance = WorkflowInstance.builder()
                .id(workflowId)
                .documentId(event.getDocumentId())
                .userId(event.getUserId() != null ? event.getUserId() : "demo-user")
                .borrowerName(event.getBorrowerName())
                .loanAmount(event.getLoanAmount())
                .annualIncome(event.getAnnualIncome())
                .status("IN_PROGRESS")
                .startedAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .build();

        instanceRepository.save(instance);
        createAuditLog(workflowId, "WORKFLOW_STARTED", "Workflow execution started for document ID: " + event.getDocumentId(), "SYSTEM");

        // Step 1: Document Analyzer Agent
        DocumentAnalyzerAgent.AnalysisResult analysis = documentAnalyzerAgent.analyze(event);
        saveStep(workflowId, "DocumentAnalyzerAgent", analysis.isValid() ? "COMPLETED" : "FAILED", analysis.getDetails());
        createAuditLog(workflowId, "DOCUMENT_ANALYZED", analysis.getDetails(), "DocumentAnalyzerAgent");

        // Step 2: Compliance Agent
        ComplianceAgent.ComplianceResult compliance = complianceAgent.checkCompliance(event);
        saveStep(workflowId, "ComplianceAgent", compliance.isPassed() ? "COMPLETED" : "FAILED", compliance.getDetails());
        createAuditLog(workflowId, "COMPLIANCE_CHECK", compliance.getDetails(), "ComplianceAgent");

        // Step 3: Decision Agent
        DecisionAgent.DecisionResult decision = decisionAgent.makeDecision(event, analysis, compliance);
        saveStep(workflowId, "DecisionAgent", "COMPLETED", "Decision: " + decision.getDecision() + ". Reason: " + decision.getReason());
        createAuditLog(workflowId, "DECISION_GENERATED", "Decision generated: " + decision.getDecision(), "DecisionAgent");

        // Finalize Workflow Instance
        instance.setStatus(decision.getDecision());
        instance.setDecision(decision.getDecision());
        instance.setDecisionReason(decision.getReason());
        instance.setCompletedAt(ZonedDateTime.now());
        instance.setUpdatedAt(ZonedDateTime.now());

        instanceRepository.save(instance);
        createAuditLog(workflowId, "WORKFLOW_COMPLETED", "Workflow completed with final decision: " + decision.getDecision(), "SYSTEM");

        // Publish Kafka Event
        WorkflowCompletedEvent completionEvent = WorkflowCompletedEvent.builder()
                .workflowId(workflowId)
                .documentId(event.getDocumentId())
                .userId(instance.getUserId())
                .borrowerName(event.getBorrowerName())
                .loanAmount(event.getLoanAmount())
                .decision(decision.getDecision())
                .decisionReason(decision.getReason())
                .timestamp(ZonedDateTime.now())
                .correlationId(event.getCorrelationId())
                .build();

        try {
            kafkaTemplate.send(TOPIC_WORKFLOW_COMPLETED, workflowId, completionEvent);
            log.info("Published WorkflowCompletedEvent for workflow ID: {}", workflowId);
        } catch (Exception e) {
            log.warn("Failed to publish WorkflowCompletedEvent: {}", e.getMessage());
        }

        return instance;
    }

    @Transactional
    public WorkflowInstance manualReview(String workflowId, String action, String reason, String reviewer) {
        WorkflowInstance instance = instanceRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow instance not found: " + workflowId));

        String newDecision = action.equalsIgnoreCase("APPROVE") ? "APPROVED" : "REJECTED";
        instance.setStatus(newDecision);
        instance.setDecision(newDecision);
        instance.setDecisionReason("Manual Underwriter Overridden: " + reason);
        instance.setUpdatedAt(ZonedDateTime.now());

        instanceRepository.save(instance);

        createAuditLog(workflowId, "MANUAL_REVIEW", "Workflow manually " + newDecision.toLowerCase() + " by " + reviewer + ". Reason: " + reason, reviewer);

        WorkflowCompletedEvent completionEvent = WorkflowCompletedEvent.builder()
                .workflowId(workflowId)
                .documentId(instance.getDocumentId())
                .userId(instance.getUserId())
                .borrowerName(instance.getBorrowerName())
                .loanAmount(instance.getLoanAmount())
                .decision(newDecision)
                .decisionReason(instance.getDecisionReason())
                .timestamp(ZonedDateTime.now())
                .build();

        try {
            kafkaTemplate.send(TOPIC_WORKFLOW_COMPLETED, workflowId, completionEvent);
        } catch (Exception e) {
            log.warn("Failed to publish manual review event: {}", e.getMessage());
        }

        return instance;
    }

    private void saveStep(String workflowId, String agentName, String status, String details) {
        WorkflowStep step = WorkflowStep.builder()
                .workflowId(workflowId)
                .agentName(agentName)
                .status(status)
                .outputDetails(details)
                .executedAt(ZonedDateTime.now())
                .build();
        stepRepository.save(step);
    }

    private void createAuditLog(String workflowId, String action, String description, String performedBy) {
        WorkflowAuditLog audit = WorkflowAuditLog.builder()
                .workflowId(workflowId)
                .action(action)
                .description(description)
                .performedBy(performedBy)
                .timestamp(ZonedDateTime.now())
                .build();
        auditLogRepository.save(audit);
    }
}
