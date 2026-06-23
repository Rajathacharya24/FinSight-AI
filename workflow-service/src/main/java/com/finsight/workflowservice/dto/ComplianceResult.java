package com.finsight.workflowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceResult {

    private boolean compliant;
    @Builder.Default
    private List<String> violations = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    private String summary;
}
