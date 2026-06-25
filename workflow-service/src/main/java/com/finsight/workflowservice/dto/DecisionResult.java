package com.finsight.workflowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResult {

    private String recommendation;
    private String rationale;
    private double confidenceScore;
}
