package com.finsight.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.notificationservice.dto.WorkflowEventDto;
import com.finsight.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Kafka consumer that listens on all three finsight workflow topics and
 * dispatches notifications via {@link NotificationService}.
 *
 * Topic constants are kept in sync with KafkaTopics.java in workflow-service
 * to avoid magic strings drifting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventConsumer {

    private static final String DOCUMENT_UPLOADED   = "finsight.document.uploaded";
    private static final String EXTRACTION_COMPLETED = "finsight.extraction.completed";
    private static final String ANALYSIS_COMPLETED  = "finsight.analysis.completed";

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = {DOCUMENT_UPLOADED, EXTRACTION_COMPLETED, ANALYSIS_COMPLETED},
        groupId = "${spring.kafka.consumer.group-id:notification-service}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onWorkflowEvent(ConsumerRecord<String, Object> record) {
        String topic = record.topic();
        Object payload = record.value();
        log.info("[NotificationConsumer] Received event on topic={} key={}", topic, record.key());

        try {
            WorkflowEventDto event = toDto(topic, payload);
            notificationService.handle(event);
        } catch (Exception e) {
            log.error("[NotificationConsumer] Failed to process event on topic={}: {}", topic, e.getMessage(), e);
            // Intentionally not re-throwing — don't poison the consumer loop.
            // A DLT can be configured here if required.
        }
    }

    /**
     * Converts the raw Kafka payload (a Map produced by workflow-service's
     * JsonSerializer) into a structured {@link WorkflowEventDto}.
     */
    @SuppressWarnings("unchecked")
    private WorkflowEventDto toDto(String topic, Object payload) {
        WorkflowEventDto dto = new WorkflowEventDto();
        dto.setTopic(topic);
        dto.setEventTimestamp(Instant.now());

        if (payload instanceof Map<?, ?> map) {
            Map<String, Object> data = (Map<String, Object>) map;

            dto.setWorkflowId(toLong(data.get("workflowId")));
            dto.setDocumentId(toLong(data.get("documentId")));
            dto.setStatus(toString(data.get("status")));
            dto.setStep(toString(data.get("step")));
            dto.setApplicantName(toString(data.get("applicantName")));
            dto.setErrorMessage(toString(data.get("errorMessage")));
        } else {
            // Fallback: attempt JSON round-trip
            try {
                dto = objectMapper.convertValue(payload, WorkflowEventDto.class);
                dto.setTopic(topic);
            } catch (Exception e) {
                log.warn("[NotificationConsumer] Could not convert payload of type {} to WorkflowEventDto",
                        payload == null ? "null" : payload.getClass().getName());
            }
        }
        return dto;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static String toString(Object v) {
        return v == null ? null : v.toString();
    }
}
