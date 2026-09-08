package com.finsight.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionDto {
    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;
    private String applicationDate;
    private String loanPurpose;
    private String employmentStatus;
    private BigDecimal confidenceScore;
}
