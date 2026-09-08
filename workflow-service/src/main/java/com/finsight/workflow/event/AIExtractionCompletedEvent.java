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
public class AIExtractionCompletedEvent {
    private String documentId;
    private String userId;
    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;
    private String applicationDate;
    private String loanPurpose;
    private String employmentStatus;
    private BigDecimal confidenceScore;
    private ZonedDateTime timestamp;
    private String correlationId;
}
