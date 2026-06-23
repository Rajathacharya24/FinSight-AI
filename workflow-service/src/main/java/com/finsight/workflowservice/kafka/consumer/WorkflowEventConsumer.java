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

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            include = {Exception.class}
    )
    @KafkaListener(topics = KafkaTopics.DOCUMENT_UPLOADED, groupId = "workflow-service")
    public void onDocumentUploaded(WorkflowEvent event) {
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
        log.info("Consumed AnalysisCompleted event: workflowId={}, documentId={}",
                event.getWorkflowId(), event.getDocumentId());
    }

    @KafkaListener(topics = {
            KafkaTopics.DOCUMENT_UPLOADED_DLT,
            KafkaTopics.EXTRACTION_COMPLETED_DLT,
            KafkaTopics.ANALYSIS_COMPLETED_DLT
    }, groupId = "workflow-service-dlt")
    public void onDeadLetter(WorkflowEvent event) {
        log.error("Dead-letter event received: type={} workflowId={} eventId={}",
                event.getEventType(), event.getWorkflowId(), event.getEventId());
    }
}
