package com.finsight.workflowservice.service.agent;

import com.finsight.workflowservice.dto.ComplianceResult;
import com.finsight.workflowservice.dto.DecisionResult;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.service.AuditLogService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DecisionAgentTest {

    private final AuditLogService auditLog = mock(AuditLogService.class);
    private final DecisionAgent agent = new DecisionAgent(auditLog);

    @Test
    void approves_when_compliant_and_no_warnings() {
        ComplianceResult compliance = ComplianceResult.builder()
                .compliant(true)
                .violations(List.of())
                .warnings(List.of())
                .build();
        ExtractionData data = ExtractionData.builder()
                .applicantName("Jane Doe")
                .loanAmount("100000")
                .build();

        DecisionResult result = agent.recommend(1L, data, compliance);

        assertThat(result.getRecommendation()).isEqualTo("APPROVE");
        assertThat(result.getConfidenceScore()).isGreaterThan(0.8);
    }

    @Test
    void recommends_review_when_warnings_present() {
        ComplianceResult compliance = ComplianceResult.builder()
                .compliant(true)
                .violations(List.of())
                .warnings(List.of("Income low"))
                .build();

        DecisionResult result = agent.recommend(1L, ExtractionData.builder().build(), compliance);

        assertThat(result.getRecommendation()).isEqualTo("REVIEW");
    }

    @Test
    void rejects_when_not_compliant() {
        ComplianceResult compliance = ComplianceResult.builder()
                .compliant(false)
                .violations(List.of("Missing income"))
                .warnings(List.of())
                .build();

        DecisionResult result = agent.recommend(1L, ExtractionData.builder().build(), compliance);

        assertThat(result.getRecommendation()).isEqualTo("REJECT");
    }
}
