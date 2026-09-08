package com.finsight.notification.service;

import com.finsight.notification.event.WorkflowCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;

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
