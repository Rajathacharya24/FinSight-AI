package com.finsight.workflowservice.controller;

import com.finsight.workflowservice.model.WorkflowInstance;
import com.finsight.workflowservice.model.WorkflowStatus;
import com.finsight.workflowservice.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final WorkflowInstanceRepository repository;

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<WorkflowInstance> all = repository.findAll();
        long completed = all.stream().filter(w -> w.getStatus() == WorkflowStatus.COMPLETED).count();
        long failed = all.stream().filter(w -> w.getStatus() == WorkflowStatus.FAILED).count();
        long inProgress = all.stream().filter(w -> w.getStatus() == WorkflowStatus.IN_PROGRESS).count();

        double avgSeconds = all.stream()
                .filter(w -> w.getCreatedAt() != null && w.getUpdatedAt() != null
                        && w.getStatus() == WorkflowStatus.COMPLETED)
                .mapToLong(w -> Duration.between(w.getCreatedAt(), w.getUpdatedAt()).toSeconds())
                .average()
                .orElse(0.0);

        double accuracy = all.isEmpty() ? 0.0 : (double) completed / all.size() * 100.0;

        return Map.of(
                "total", all.size(),
                "completed", completed,
                "failed", failed,
                "inProgress", inProgress,
                "averageProcessingSeconds", avgSeconds,
                "accuracyPercent", accuracy,
                "estimatedCostUsd", all.size() * 0.045
        );
    }

    @GetMapping("/timeseries")
    public List<Map<String, Object>> timeseries() {
        return repository.findAll().stream()
                .filter(w -> w.getStatus() == WorkflowStatus.COMPLETED)
                .map(w -> Map.<String, Object>of(
                        "workflowId", w.getId(),
                        "createdAt", w.getCreatedAt(),
                        "completedAt", w.getUpdatedAt(),
                        "durationSeconds",
                            Duration.between(w.getCreatedAt(), w.getUpdatedAt()).toSeconds()))
                .toList();
    }
}
