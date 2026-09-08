package com.finsight.notification.controller;

import com.finsight.notification.domain.Notification;
import com.finsight.notification.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId != null && !userId.isBlank()) {
            return ResponseEntity.ok(notificationRepository.findByUserIdOrderBySentAtDesc(userId));
        }
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
