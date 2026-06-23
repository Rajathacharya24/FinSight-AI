package com.finsight.workflowservice.service.agent;

import com.finsight.workflowservice.dto.ComplianceResult;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.service.AuditLogService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ComplianceAgentTest {

    private final AuditLogService auditLog = mock(AuditLogService.class);
    private final ComplianceAgent agent = new ComplianceAgent(auditLog);

    @Test
    void approves_compliant_application() {
        ExtractionData data = ExtractionData.builder()
                .applicantName("Jane Doe")
                .address("123 Main St")
                .income("80000")
                .loanAmount("100000")
                .build();

        ComplianceResult result = agent.validate(1L, data);

        assertThat(result.isCompliant()).isTrue();
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    void rejects_when_loan_to_income_too_high() {
        ExtractionData data = ExtractionData.builder()
                .applicantName("Jane Doe")
                .address("123 Main St")
                .income("30000")
                .loanAmount("200000")
                .build();

        ComplianceResult result = agent.validate(1L, data);

        assertThat(result.isCompliant()).isFalse();
        assertThat(result.getViolations()).anyMatch(v -> v.contains("ratio"));
    }

    @Test
    void flags_missing_fields() {
        ExtractionData data = ExtractionData.builder().build();

        ComplianceResult result = agent.validate(1L, data);

        assertThat(result.isCompliant()).isFalse();
        assertThat(result.getViolations()).contains("Applicant name is missing");
        assertThat(result.getViolations()).contains("Address is missing");
    }
}
