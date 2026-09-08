package com.finsight.notification.domain;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    private String status;

    private ZonedDateTime sentAt = ZonedDateTime.now();

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public ZonedDateTime getSentAt() { return sentAt; }
    public void setSentAt(ZonedDateTime sentAt) { this.sentAt = sentAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String workflowId;
        private String userId;
        private String recipientEmail;
        private String subject;
        private String message;
        private String status;
        private ZonedDateTime sentAt = ZonedDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder workflowId(String workflowId) { this.workflowId = workflowId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder recipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder sentAt(ZonedDateTime sentAt) { this.sentAt = sentAt; return this; }

        public Notification build() {
            Notification n = new Notification();
            n.setId(id);
            n.setWorkflowId(workflowId);
            n.setUserId(userId);
            n.setRecipientEmail(recipientEmail);
            n.setSubject(subject);
            n.setMessage(message);
            n.setStatus(status);
            n.setSentAt(sentAt);
            return n;
        }
    }
}
