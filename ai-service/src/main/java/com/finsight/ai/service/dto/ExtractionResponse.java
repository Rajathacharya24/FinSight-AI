package com.finsight.ai.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResponse {
    private Long documentId;
    private String applicantName;
    private String income;
    private String address;
    private String loanAmount;
    private String status;
}
