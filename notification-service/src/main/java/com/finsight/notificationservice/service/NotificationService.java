package com.finsight.notificationservice.service;

import com.finsight.notificationservice.dto.WorkflowEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Core notification dispatcher.
 *
 * <p>Supports two delivery channels:
 * <ul>
 *   <li><strong>Email</strong> — via JavaMailSender (real SMTP or StubMailSender in dev)</li>
 *   <li><strong>Webhook</strong> — HTTP POST to NOTIFICATION_WEBHOOK_URL if set</li>
 * </ul>
 *
 * <p>Both are executed asynchronously so the Kafka listener returns immediately.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final WebClient.Builder webClientBuilder;

    @Value("${notification.from-address:noreply@finsight.ai}")
    private String fromAddress;

    @Value("${notification.to-address:}")
    private String defaultToAddress;

    @Value("${notification.webhook-url:}")
    private String webhookUrl;

    /**
     * Main entry point — routes event to the appropriate notification channels.
     */
    @Async
    public void handle(WorkflowEventDto event) {
        log.info("[NotificationService] Handling event: topic={} workflowId={} status={}",
                event.getTopic(), event.getWorkflowId(), event.getStatus());

        sendEmail(event);
        sendWebhook(event);
    }

    // ─────────────────────── Email ───────────────────────

    private void sendEmail(WorkflowEventDto event) {
        if (defaultToAddress == null || defaultToAddress.isBlank()) {
            log.debug("[NotificationService] No to-address configured — skipping email");
            return;
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(defaultToAddress);
        msg.setSubject(buildSubject(event));
        msg.setText(buildBody(event));

        try {
            mailSender.send(msg);
            log.info("[NotificationService] Email sent for workflowId={}", event.getWorkflowId());
        } catch (Exception e) {
            log.error("[NotificationService] Email delivery failed for workflowId={}: {}",
                    event.getWorkflowId(), e.getMessage());
        }
    }

    private String buildSubject(WorkflowEventDto event) {
        return switch (event.getTopic()) {
            case "finsight.document.uploaded"   -> "📄 Document Uploaded — Workflow #" + event.getWorkflowId();
            case "finsight.extraction.completed" -> "🔍 Extraction Complete — Workflow #" + event.getWorkflowId();
            case "finsight.analysis.completed"  -> "✅ Analysis Complete — Workflow #" + event.getWorkflowId();
            default                             -> "FinSight AI — Workflow #" + event.getWorkflowId() + " Update";
        };
    }

    private String buildBody(WorkflowEventDto event) {
        StringBuilder sb = new StringBuilder();
        sb.append("FinSight AI — Workflow Notification\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("Workflow ID : ").append(event.getWorkflowId()).append("\n");
        if (event.getDocumentId() != null)
            sb.append("Document ID : ").append(event.getDocumentId()).append("\n");
        if (event.getStatus() != null)
            sb.append("Status      : ").append(event.getStatus()).append("\n");
        if (event.getStep() != null)
            sb.append("Step        : ").append(event.getStep()).append("\n");
        if (event.getApplicantName() != null)
            sb.append("Applicant   : ").append(event.getApplicantName()).append("\n");
        if (event.getErrorMessage() != null)
            sb.append("Error       : ").append(event.getErrorMessage()).append("\n");
        sb.append("Timestamp   : ").append(event.getEventTimestamp()).append("\n");
        return sb.toString();
    }

    // ─────────────────────── Webhook ───────────────────────

    private void sendWebhook(WorkflowEventDto event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("[NotificationService] No webhook URL configured — skipping webhook");
            return;
        }

        Map<String, Object> payload = Map.of(
                "topic",        event.getTopic(),
                "workflowId",   event.getWorkflowId(),
                "documentId",   event.getDocumentId() != null ? event.getDocumentId() : 0,
                "status",       event.getStatus() != null ? event.getStatus() : "",
                "step",         event.getStep() != null ? event.getStep() : "",
                "eventTimestamp", event.getEventTimestamp().toString()
        );

        try {
            webClientBuilder.build()
                    .post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(r -> log.info("[NotificationService] Webhook delivered for workflowId={} status={}",
                            event.getWorkflowId(), r.getStatusCode()))
                    .doOnError(e -> log.error("[NotificationService] Webhook failed for workflowId={}: {}",
                            event.getWorkflowId(), e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("[NotificationService] Failed to dispatch webhook: {}", e.getMessage());
        }
    }
}
