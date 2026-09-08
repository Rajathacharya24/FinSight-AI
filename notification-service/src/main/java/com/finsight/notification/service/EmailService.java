package com.finsight.notification.service;

import com.finsight.notification.domain.Notification;
import com.finsight.notification.event.WorkflowCompletedEvent;
import com.finsight.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Value("${spring.mail.username:noreply@finsight.ai}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }

    public void processAndSendNotification(WorkflowCompletedEvent event) {
        String recipientEmail = "demo@finsight.ai";
        String subject = "FinSight AI Decision: Application " + event.getDecision() + " (Doc: " + event.getDocumentId().substring(0, 8) + ")";
        
        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; padding: 20px; color: #1e293b;">
                <h2 style="color: #0f172a;">FinSight AI Financial Document Intelligence</h2>
                <hr style="border: 1px solid #e2e8f0;"/>
                <p>Hello <strong>%s</strong>,</p>
                <p>Your loan application document has been processed by our multi-agent automated compliance workflow engine.</p>
                <div style="background-color: #f8fafc; border-left: 4px solid %s; padding: 15px; margin: 15px 0;">
                    <p style="margin: 0; font-size: 18px; font-weight: bold;">Status: %s</p>
                    <p style="margin: 5px 0 0 0; color: #475569;">%s</p>
                </div>
                <table style="width: 100%%; border-collapse: collapse; margin-top: 15px;">
                    <tr><td style="padding: 8px; font-weight: bold;">Applicant Name:</td><td>%s</td></tr>
                    <tr><td style="padding: 8px; font-weight: bold;">Requested Amount:</td><td>$%.2f</td></tr>
                    <tr><td style="padding: 8px; font-weight: bold;">Workflow ID:</td><td>%s</td></tr>
                </table>
                <p style="margin-top: 25px; font-size: 12px; color: #94a3b8;">This is an automated notification sent by FinSight AI Microservices Platform.</p>
            </div>
            """,
                event.getBorrowerName() != null ? event.getBorrowerName() : "Applicant",
                "APPROVED".equals(event.getDecision()) ? "#16a34a" : ("REJECTED".equals(event.getDecision()) ? "#dc2626" : "#d97706"),
                event.getDecision(),
                event.getDecisionReason(),
                event.getBorrowerName() != null ? event.getBorrowerName() : "N/A",
                event.getLoanAmount() != null ? event.getLoanAmount().doubleValue() : 0.0,
                event.getWorkflowId()
        );

        String status = "SENT";
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Sent email notification to {} for workflow ID {}", recipientEmail, event.getWorkflowId());
        } catch (Exception e) {
            log.warn("Failed to send email via Mailpit (SMTP). Saving notification record: {}", e.getMessage());
            status = "FAILED";
        }

        Notification notification = Notification.builder()
                .workflowId(event.getWorkflowId())
                .userId(event.getUserId() != null ? event.getUserId() : "demo-user")
                .recipientEmail(recipientEmail)
                .subject(subject)
                .message(htmlContent)
                .status(status)
                .sentAt(ZonedDateTime.now())
                .build();

        notificationRepository.save(notification);
    }
}
