package com.finsight.notificationservice.controller;

import com.finsight.notificationservice.model.Notification;
import com.finsight.notificationservice.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Notification management endpoints.
 * Provides in-app notification history and read-state management.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification service — history, read-state, and service status")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @Value("${notification.from-address:noreply@finsight.ai}")
    private String fromAddress;

    @Value("${notification.webhook-url:}")
    private String webhookUrl;

    @Value("${spring.mail.host:}")
    private String smtpHost;

    // ─────────────────────────────────────────────────────────────────────────
    // Service status (keep existing)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/status")
    @Operation(summary = "Return notification service configuration status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "service",           "notification-service",
                "timestamp",         Instant.now().toString(),
                "emailMode",         smtpHost.isBlank() ? "STUB_LOG" : "SMTP",
                "webhookConfigured", !webhookUrl.isBlank(),
                "fromAddress",       fromAddress,
                "topics", new String[]{
                        "finsight.document.uploaded",
                        "finsight.extraction.completed",
                        "finsight.analysis.completed"
                }
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /{userId} — paginated notification history
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get notifications for a user (paginated)")
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        long unreadCount = notificationRepository.countByUserIdAndReadAtIsNull(userId);

        return ResponseEntity.ok(Map.of(
                "content",       result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages",    result.getTotalPages(),
                "page",          result.getNumber(),
                "size",          result.getSize(),
                "unreadCount",   unreadCount
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /{id}/read — mark single notification as read
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Mark a notification as read (idempotent)")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(n -> {
                    if (n.getReadAt() == null) {
                        n.setReadAt(Instant.now());
                        notificationRepository.save(n);
                    }
                    return ResponseEntity.ok(n);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /{userId}/read-all — mark all unread as read
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Mark all unread notifications as read for a user")
    @PatchMapping("/{userId}/read-all")
    public ResponseEntity<Map<String, Long>> markAllRead(@PathVariable Long userId) {
        Page<Notification> allNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, 500));
        Instant now = Instant.now();
        var toUpdate = allNotifications.stream()
                .filter(n -> n.getReadAt() == null)
                .peek(n -> n.setReadAt(now))
                .toList();
        notificationRepository.saveAll(toUpdate);
        return ResponseEntity.ok(Map.of("marked", (long) toUpdate.size()));
    }
}
