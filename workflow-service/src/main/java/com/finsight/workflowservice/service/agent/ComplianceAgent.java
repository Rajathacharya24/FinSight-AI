package com.finsight.workflowservice.service.agent;

import com.finsight.workflowservice.dto.ComplianceResult;
import com.finsight.workflowservice.dto.ExtractionData;
import com.finsight.workflowservice.model.AgentType;
import com.finsight.workflowservice.model.WorkflowStep;
import com.finsight.workflowservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ComplianceAgent {

    private static final double MAX_LOAN_TO_INCOME_RATIO = 3.0;
    private static final double MIN_INCOME = 25_000;

    private final AuditLogService auditLogService;

    public ComplianceResult validate(Long workflowId, ExtractionData extraction) {
        auditLogService.logStarted(workflowId, WorkflowStep.VALIDATE, AgentType.COMPLIANCE_AGENT,
                "validate_loan_application", extraction);

        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (isBlank(extraction.getApplicantName())) {
            violations.add("Applicant name is missing");
        }
        if (isBlank(extraction.getAddress())) {
            violations.add("Address is missing");
        }

        Double income = parseAmount(extraction.getIncome());
        Double loanAmount = parseAmount(extraction.getLoanAmount());

        if (income == null || income <= 0) {
            violations.add("Valid income is required");
        } else if (income < MIN_INCOME) {
            warnings.add("Income below recommended minimum of " + MIN_INCOME);
        }

        if (loanAmount == null || loanAmount <= 0) {
            violations.add("Valid loan amount is required");
        } else if (income != null && income > 0) {
            double ratio = loanAmount / income;
            if (ratio > MAX_LOAN_TO_INCOME_RATIO) {
                violations.add(String.format("Loan-to-income ratio %.2f exceeds maximum %.2f",
                        ratio, MAX_LOAN_TO_INCOME_RATIO));
            } else if (ratio > MAX_LOAN_TO_INCOME_RATIO * 0.8) {
                warnings.add(String.format("Loan-to-income ratio %.2f is near the limit", ratio));
            }
        }

        boolean compliant = violations.isEmpty();
        String summary = compliant
                ? "Application meets compliance requirements"
                : "Application failed " + violations.size() + " compliance check(s)";

        ComplianceResult result = ComplianceResult.builder()
                .compliant(compliant)
                .violations(violations)
                .warnings(warnings)
                .summary(summary)
                .build();

        auditLogService.logCompleted(workflowId, WorkflowStep.VALIDATE, AgentType.COMPLIANCE_AGENT,
                "validate_loan_application", result);
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Double parseAmount(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
