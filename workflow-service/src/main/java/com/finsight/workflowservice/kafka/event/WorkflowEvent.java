package com.finsight.workflowservice.kafka.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowEvent {

    private String eventId;
    private String eventType;
    private Long workflowId;
    private Long documentId;
    private String status;
    private Object payload;
    private Instant occurredAt;
}
