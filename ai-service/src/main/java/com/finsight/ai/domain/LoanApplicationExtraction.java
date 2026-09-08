package com.finsight.ai.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "loan_application_extractions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationExtraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String documentId;

    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal annualIncome;
    private String applicationDate;
    private String loanPurpose;
    private String employmentStatus;
    private BigDecimal confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String rawExtractionJson;

    @Builder.Default
    private ZonedDateTime createdAt = ZonedDateTime.now();
}
