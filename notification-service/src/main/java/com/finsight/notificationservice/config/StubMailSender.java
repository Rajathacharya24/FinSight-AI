package com.finsight.notificationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Stub JavaMailSender that logs email content instead of sending it.
 * Used in dev/test environments when no SMTP host is configured.
 */
@Slf4j
class StubMailSender implements JavaMailSender {

    @Override
    public MimeMessage createMimeMessage() {
        try {
            return new MimeMessage((jakarta.mail.Session) null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create MimeMessage", e);
        }
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return createMimeMessage();
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        log.info("[StubMailSender] Would send MimeMessage");
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        log.info("[StubMailSender] Would send {} MimeMessage(s)", mimeMessages.length);
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        log.info("[StubMailSender] Would send MimeMessagePreparator");
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        log.info("[StubMailSender] Would send {} MimeMessagePreparator(s)", mimeMessagePreparators.length);
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        log.info("[StubMailSender] 📧 STUB EMAIL — To: {} | Subject: {} | Body: {}",
                Arrays.toString(simpleMessage.getTo()),
                simpleMessage.getSubject(),
                simpleMessage.getText());
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        for (SimpleMailMessage msg : simpleMessages) {
            send(msg);
        }
    }
}
