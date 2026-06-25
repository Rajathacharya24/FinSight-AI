package com.finsight.ai.service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "loan_application_extraction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationExtraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "applicant_name")
    private String applicantName;

    @Column(name = "income")
    private String income;

    @Column(name = "address")
    private String address;

    @Column(name = "loan_amount")
    private String loanAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
