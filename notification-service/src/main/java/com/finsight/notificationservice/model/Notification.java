package com.finsight.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Persisted in-app notification record.
 * Stored whenever an email/webhook is dispatched so users can view
 * their notification history via GET /api/v1/notifications/{userId}.
 */
@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Maps to the userId from the workflow event (or null for system notifications). */
    @Column(name = "user_id")
    private Long userId;

    /** The workflow this notification is about. */
    @Column(name = "workflow_id")
    private Long workflowId;

    /** Kafka topic that triggered this notification. */
    @Column(name = "topic", length = 100)
    private String topic;

    /** Human-readable notification title/subject. */
    @Column(name = "title", length = 255)
    private String title;

    /** Full notification message body. */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Delivery channel: EMAIL, WEBHOOK, IN_APP.
     * A single event may create multiple records (one per channel).
     */
    @Column(name = "channel", length = 20)
    private String channel;

    /** Whether the user has read/dismissed this notification in the UI. */
    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
