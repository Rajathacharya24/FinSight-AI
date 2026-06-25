package com.finsight.workflowservice.kafka.producer;

import com.finsight.workflowservice.kafka.KafkaTopics;
import com.finsight.workflowservice.kafka.event.WorkflowEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventProducer {

    private final KafkaTemplate<String, WorkflowEvent> kafkaTemplate;

    public void publishDocumentUploaded(Long workflowId, Long documentId, Object payload) {
        publish(KafkaTopics.DOCUMENT_UPLOADED, "DocumentUploaded", workflowId, documentId, payload);
    }

    public void publishExtractionCompleted(Long workflowId, Long documentId, Object payload) {
        publish(KafkaTopics.EXTRACTION_COMPLETED, "ExtractionCompleted", workflowId, documentId, payload);
    }

    public void publishAnalysisCompleted(Long workflowId, Long documentId, Object payload) {
        publish(KafkaTopics.ANALYSIS_COMPLETED, "AnalysisCompleted", workflowId, documentId, payload);
    }

    private void publish(String topic, String eventType, Long workflowId, Long documentId, Object payload) {
        WorkflowEvent event = WorkflowEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .workflowId(workflowId)
                .documentId(documentId)
                .status("OK")
                .payload(payload)
                .occurredAt(Instant.now())
                .build();

        String key = workflowId != null ? workflowId.toString() : event.getEventId();
        CompletableFuture<SendResult<String, WorkflowEvent>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for workflow {}: {}", eventType, workflowId, ex.getMessage(), ex);
            } else {
                log.info("Published {} to {} (offset={}, partition={})",
                        eventType, topic,
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}
