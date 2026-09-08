package com.finsight.notification.service;

import com.finsight.notification.event.WorkflowCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "workflow-completed-topic", groupId = "notification-service-group")
    public void handleWorkflowCompleted(WorkflowCompletedEvent event) {
        log.info("Received WorkflowCompletedEvent for workflow ID: {}", event.getWorkflowId());
        try {
            emailService.processAndSendNotification(event);
        } catch (Exception e) {
            log.error("Error processing notification for workflow ID: {}", event.getWorkflowId(), e);
        }
    }
}
