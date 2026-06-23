package com.finsight.workflowservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.workflowservice.dto.*;
import com.finsight.workflowservice.exception.WorkflowException;
import com.finsight.workflowservice.kafka.producer.WorkflowEventProducer;
import com.finsight.workflowservice.model.WorkflowInstance;
import com.finsight.workflowservice.model.WorkflowStatus;
import com.finsight.workflowservice.model.WorkflowStep;
import com.finsight.workflowservice.repository.WorkflowInstanceRepository;
import com.finsight.workflowservice.service.agent.ComplianceAgent;
import com.finsight.workflowservice.service.agent.DecisionAgent;
import com.finsight.workflowservice.service.agent.DocumentAnalyzerAgent;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class WorkflowOrchestrator {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AuditLogService auditLogService;
    private final DocumentAnalyzerAgent documentAnalyzerAgent;
    private final ComplianceAgent complianceAgent;
    private final DecisionAgent decisionAgent;
    private final ObjectMapper objectMapper;
    private final WorkflowEventProducer eventProducer;

    @Transactional
    @Timed(value = "workflow.execute", description = "Time to execute a full workflow")
    public WorkflowResponse execute(String title, String description, MultipartFile file, Long userId) {
        WorkflowInstance instance = createInstance(userId != null ? userId : 0L);
        Long workflowId = instance.getId();

        try {
            instance.setStatus(WorkflowStatus.IN_PROGRESS);
            instance.setCurrentStep(WorkflowStep.UPLOAD);
            workflowInstanceRepository.save(instance);

            DocumentUploadResponse upload = documentAnalyzerAgent.upload(workflowId, title, description, file);
            instance.setDocumentId(upload.getDocumentId());
            instance.setCurrentStep(WorkflowStep.EXTRACT);
            workflowInstanceRepository.save(instance);
            eventProducer.publishDocumentUploaded(workflowId, upload.getDocumentId(), upload);

            String extractedText = documentAnalyzerAgent.fetchExtractedText(upload.getDocumentId());
            ExtractionData extraction = documentAnalyzerAgent.extract(workflowId, upload.getDocumentId(), extractedText);
            instance.setExtractionData(toJson(extraction));
            instance.setCurrentStep(WorkflowStep.VALIDATE);
            workflowInstanceRepository.save(instance);
            eventProducer.publishExtractionCompleted(workflowId, upload.getDocumentId(), extraction);

            ComplianceResult compliance = complianceAgent.validate(workflowId, extraction);
            instance.setComplianceData(toJson(compliance));
            instance.setCurrentStep(WorkflowStep.RECOMMEND);
            workflowInstanceRepository.save(instance);

            DecisionResult decision = decisionAgent.recommend(workflowId, extraction, compliance);
            instance.setDecisionData(toJson(decision));
            instance.setCurrentStep(WorkflowStep.RECOMMEND);
            instance.setStatus(WorkflowStatus.COMPLETED);
            workflowInstanceRepository.save(instance);
            eventProducer.publishAnalysisCompleted(workflowId, upload.getDocumentId(),
                    java.util.Map.of("compliance", compliance, "decision", decision));

            return buildResponse(instance, extraction, compliance, decision);
        } catch (Exception e) {
            instance.setStatus(WorkflowStatus.FAILED);
            instance.setErrorMessage(e.getMessage());
            workflowInstanceRepository.save(instance);
            throw e instanceof WorkflowException workflowException
                    ? workflowException
                    : new WorkflowException("Workflow execution failed", e);
        }
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(Long workflowId) {
        WorkflowInstance instance = workflowInstanceRepository.findById(workflowId)
                .orElseThrow(() -> new WorkflowException("Workflow not found: " + workflowId));

        return buildResponse(
                instance,
                parseJson(instance.getExtractionData(), ExtractionData.class),
                parseJson(instance.getComplianceData(), ComplianceResult.class),
                parseJson(instance.getDecisionData(), DecisionResult.class));
    }

    @Transactional(readOnly = true)
    public java.util.List<WorkflowResponse> listWorkflows() {
        return workflowInstanceRepository.findAll().stream()
                .map(instance -> buildResponse(
                        instance,
                        parseJson(instance.getExtractionData(), ExtractionData.class),
                        parseJson(instance.getComplianceData(), ComplianceResult.class),
                        parseJson(instance.getDecisionData(), DecisionResult.class)))
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<AuditLogEntryDto> getAuditLog(Long workflowId) {
        if (!workflowInstanceRepository.existsById(workflowId)) {
            throw new WorkflowException("Workflow not found: " + workflowId);
        }
        return auditLogService.getAuditLog(workflowId);
    }

    private WorkflowInstance createInstance(Long userId) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setStatus(WorkflowStatus.CREATED);
        instance.setUserId(userId);
        return workflowInstanceRepository.save(instance);
    }

    private WorkflowResponse buildResponse(WorkflowInstance instance,
                                           ExtractionData extraction,
                                           ComplianceResult compliance,
                                           DecisionResult decision) {
        return WorkflowResponse.builder()
                .workflowId(instance.getId())
                .documentId(instance.getDocumentId())
                .status(instance.getStatus())
                .currentStep(instance.getCurrentStep())
                .extraction(extraction)
                .compliance(compliance)
                .decision(decision)
                .auditLog(auditLogService.getAuditLog(instance.getId()))
                .createdAt(instance.getCreatedAt())
                .updatedAt(instance.getUpdatedAt())
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new WorkflowException("Failed to serialize workflow state", e);
        }
    }

    private <T> T parseJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new WorkflowException("Failed to deserialize workflow state", e);
        }
    }
}
