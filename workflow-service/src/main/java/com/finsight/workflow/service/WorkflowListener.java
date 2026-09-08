package com.finsight.workflow.service;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WorkflowListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowListener.class);
    private final WorkflowOrchestrator workflowOrchestrator;

    public WorkflowListener(WorkflowOrchestrator workflowOrchestrator) {
        this.workflowOrchestrator = workflowOrchestrator;
    }

    @KafkaListener(topics = "ai-extraction-completed-topic", groupId = "workflow-service-group")
    public void handleAiExtractionCompleted(AIExtractionCompletedEvent event) {
        log.info("Received AIExtractionCompletedEvent for document ID: {}", event.getDocumentId());
        try {
            workflowOrchestrator.executeWorkflow(event);
        } catch (Exception e) {
            log.error("Error executing workflow for document ID: {}", event.getDocumentId(), e);
        }
    }
}
