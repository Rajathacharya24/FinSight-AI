package com.finsight.workflowservice.kafka;

import com.finsight.workflowservice.kafka.event.WorkflowEvent;
import com.finsight.workflowservice.kafka.producer.WorkflowEventProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowEventProducerTest {

    @Test
    @SuppressWarnings("unchecked")
    void publishes_document_uploaded_with_correct_payload() {
        KafkaTemplate<String, WorkflowEvent> template = mock(KafkaTemplate.class);
        RecordMetadata md = new RecordMetadata(new TopicPartition("t", 0), 0, 0, 0, 0, 0);
        SendResult<String, WorkflowEvent> result = new SendResult<>(null, md);
        when(template.send(anyString(), anyString(), any(WorkflowEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(result));

        WorkflowEventProducer producer = new WorkflowEventProducer(template);
        producer.publishDocumentUploaded(42L, 99L, "payload");

        ArgumentCaptor<WorkflowEvent> captor = ArgumentCaptor.forClass(WorkflowEvent.class);
        verify(template).send(eqTopic(KafkaTopics.DOCUMENT_UPLOADED), eqKey("42"), captor.capture());

        WorkflowEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo("DocumentUploaded");
        assertThat(event.getWorkflowId()).isEqualTo(42L);
        assertThat(event.getDocumentId()).isEqualTo(99L);
        assertThat(event.getEventId()).isNotBlank();
    }

    private static String eqTopic(String s) { return org.mockito.ArgumentMatchers.eq(s); }
    private static String eqKey(String s) { return org.mockito.ArgumentMatchers.eq(s); }
}
