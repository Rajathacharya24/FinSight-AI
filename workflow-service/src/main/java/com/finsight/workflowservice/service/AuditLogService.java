package com.finsight.workflowservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.workflowservice.dto.AuditLogEntryDto;
import com.finsight.workflowservice.model.*;
import com.finsight.workflowservice.repository.WorkflowAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final WorkflowAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logStarted(Long workflowId, WorkflowStep step, AgentType agent, String action, Object input) {
        save(workflowId, step, agent, action, AuditStatus.STARTED, input, null);
    }

    @Transactional
    public void logCompleted(Long workflowId, WorkflowStep step, AgentType agent, String action, Object output) {
        save(workflowId, step, agent, action, AuditStatus.COMPLETED, null, output);
    }

    @Transactional
    public void logFailed(Long workflowId, WorkflowStep step, AgentType agent, String action, Object input, String error) {
        save(workflowId, step, agent, action, AuditStatus.FAILED, input, error);
    }

    @Transactional(readOnly = true)
    public List<AuditLogEntryDto> getAuditLog(Long workflowId) {
        return auditLogRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId).stream()
                .map(this::toDto)
                .toList();
    }

    private void save(Long workflowId, WorkflowStep step, AgentType agent, String action,
                      AuditStatus status, Object input, Object output) {
        WorkflowAuditLog entry = new WorkflowAuditLog();
        entry.setWorkflowId(workflowId);
        entry.setStep(step);
        entry.setAgent(agent);
        entry.setAction(action);
        entry.setStatus(status);
        entry.setInputSummary(toJson(input));
        entry.setOutputSummary(toJson(output));
        auditLogRepository.save(entry);
    }

    private AuditLogEntryDto toDto(WorkflowAuditLog log) {
        return AuditLogEntryDto.builder()
                .id(log.getId())
                .step(log.getStep())
                .agent(log.getAgent())
                .action(log.getAction())
                .status(log.getStatus())
                .inputSummary(log.getInputSummary())
                .outputSummary(log.getOutputSummary())
                .timestamp(log.getCreatedAt())
                .build();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String string) {
            return string;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return value.toString();
        }
    }
}
