package com.finsight.workflowservice.kafka.consumer;

import com.finsight.workflowservice.kafka.KafkaTopics;
import com.finsight.workflowservice.kafka.event.WorkflowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkflowEventConsumer {

    private final com.finsight.workflowservice.repository.WorkflowInstanceRepository workflowInstanceRepository;

    public WorkflowEventConsumer(com.finsight.workflowservice.repository.WorkflowInstanceRepository workflowInstanceRepository) {
        this.workflowInstanceRepository = workflowInstanceRepository;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            include = {Exception.class}
    )
    @KafkaListener(topics = KafkaTopics.DOCUMENT_UPLOADED, groupId = "workflow-service")
    public void onDocumentUploaded(WorkflowEvent event) {
        if (isAlreadyProcessed(event.getWorkflowId(), com.finsight.workflowservice.model.WorkflowStep.UPLOAD)) {
            log.info("Idempotency: Skipping already processed DocumentUploaded event: {}", event.getEventId());
            return;
        }
        log.info("Consumed DocumentUploaded event: workflowId={}, documentId={}",
                event.getWorkflowId(), event.getDocumentId());
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = KafkaTopics.EXTRACTION_COMPLETED, groupId = "workflow-service")
    public void onExtractionCompleted(WorkflowEvent event) {
        if (isAlreadyProcessed(event.getWorkflowId(), com.finsight.workflowservice.model.WorkflowStep.EXTRACT)) {
            log.info("Idempotency: Skipping already processed ExtractionCompleted event: {}", event.getEventId());
            return;
        }
        log.info("Consumed ExtractionCompleted event: workflowId={}, documentId={}",
                event.getWorkflowId(), event.getDocumentId());
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = KafkaTopics.ANALYSIS_COMPLETED, groupId = "workflow-service")
    public void onAnalysisCompleted(WorkflowEvent event) {
        if (isAlreadyProcessed(event.getWorkflowId(), com.finsight.workflowservice.model.WorkflowStep.RECOMMEND)) {
            log.info("Idempotency: Skipping already processed AnalysisCompleted event: {}", event.getEventId());
            return;
        }
        log.info("Consumed AnalysisCompleted event: workflowId={}, documentId={}",
                event.getWorkflowId(), event.getDocumentId());
    }

    @KafkaListener(topics = {
            KafkaTopics.DOCUMENT_UPLOADED + "-dlt",
            KafkaTopics.EXTRACTION_COMPLETED + "-dlt",
            KafkaTopics.ANALYSIS_COMPLETED + "-dlt"
    }, groupId = "workflow-service-dlt")
    public void onDeadLetter(WorkflowEvent event) {
        log.error("ALERT: Dead-letter event received: type={} workflowId={} eventId={}. Parking event for manual replay.",
                event.getEventType(), event.getWorkflowId(), event.getEventId());
        
        workflowInstanceRepository.findById(event.getWorkflowId()).ifPresent(instance -> {
            instance.setStatus(com.finsight.workflowservice.model.WorkflowStatus.FAILED);
            instance.setErrorMessage("Failed to process event: " + event.getEventType() + ". Event parked in DLQ.");
            workflowInstanceRepository.save(instance);
        });
    }

    private boolean isAlreadyProcessed(Long workflowId, com.finsight.workflowservice.model.WorkflowStep currentEventStep) {
        return workflowInstanceRepository.findById(workflowId)
                .map(instance -> instance.getCurrentStep().ordinal() > currentEventStep.ordinal())
                .orElse(false);
    }
}
