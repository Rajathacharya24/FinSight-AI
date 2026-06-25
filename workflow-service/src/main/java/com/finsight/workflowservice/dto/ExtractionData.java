package com.finsight.workflowservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionData {

    private Long documentId;
    private String applicantName;
    private String income;
    private String address;
    private String loanAmount;
    private String status;
}
