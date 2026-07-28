package com.finsight.notificationservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.notificationservice.dto.WorkflowEventDto;
import com.finsight.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    private WorkflowEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new WorkflowEventConsumer(notificationService, new ObjectMapper());
    }

    @Test
    void onWorkflowEvent_analysisCompleted_dispatchesNotification() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("workflowId", 42L);
        payload.put("documentId", 7L);
        payload.put("status", "COMPLETED");
        payload.put("step", "RECOMMEND");

        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "finsight.analysis.completed", 0, 0, "key-42", payload);

        consumer.onWorkflowEvent(record);

        ArgumentCaptor<WorkflowEventDto> captor = ArgumentCaptor.forClass(WorkflowEventDto.class);
        verify(notificationService).handle(captor.capture());

        WorkflowEventDto dto = captor.getValue();
        assertThat(dto.getWorkflowId()).isEqualTo(42L);
        assertThat(dto.getDocumentId()).isEqualTo(7L);
        assertThat(dto.getStatus()).isEqualTo("COMPLETED");
        assertThat(dto.getTopic()).isEqualTo("finsight.analysis.completed");
    }

    @Test
    void onWorkflowEvent_invalidPayload_doesNotThrow() {
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(
                "finsight.document.uploaded", 0, 0, "bad-key", "not-a-map");

        // Should not throw — consumer must be resilient
        consumer.onWorkflowEvent(record);
    }
}
