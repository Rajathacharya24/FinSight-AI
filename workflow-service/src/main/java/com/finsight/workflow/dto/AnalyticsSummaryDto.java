package com.finsight.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private long totalWorkflows;
    private long approvedCount;
    private long rejectedCount;
    private long needsReviewCount;
    private BigDecimal approvalRate;
    private BigDecimal averageLoanAmount;
    private double averageProcessingTimeSeconds;
    private Map<String, Long> statusDistribution;
}
