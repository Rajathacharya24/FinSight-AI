package com.finsight.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Lightweight notification status endpoint.
 * Actuator /actuator/health covers the Kafka consumer binding;
 * this endpoint exposes service-level metadata for quick verification.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification service health and metadata")
public class NotificationController {

    @Value("${notification.from-address:noreply@finsight.ai}")
    private String fromAddress;

    @Value("${notification.webhook-url:}")
    private String webhookUrl;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @GetMapping("/status")
    @Operation(summary = "Return notification service configuration status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "service",          "notification-service",
                "timestamp",        Instant.now().toString(),
                "emailMode",        smtpHost.isBlank() ? "STUB_LOG" : "SMTP",
                "webhookConfigured", !webhookUrl.isBlank(),
                "fromAddress",      fromAddress,
                "topics", new String[]{
                        "finsight.document.uploaded",
                        "finsight.extraction.completed",
                        "finsight.analysis.completed"
                }
        ));
    }
}
