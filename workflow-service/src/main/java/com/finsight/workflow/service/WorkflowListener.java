package com.finsight.workflow.service;

import com.finsight.workflow.event.AIExtractionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowListener {

    private final WorkflowOrchestrator workflowOrchestrator;

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
