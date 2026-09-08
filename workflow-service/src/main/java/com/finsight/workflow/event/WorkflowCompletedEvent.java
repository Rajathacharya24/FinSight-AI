package com.finsight.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCompletedEvent {
    private String workflowId;
    private String documentId;
    private String userId;
    private String borrowerName;
    private BigDecimal loanAmount;
    private String decision;
    private String decisionReason;
    private ZonedDateTime timestamp;
    private String correlationId;
}
