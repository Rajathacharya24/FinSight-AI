package com.finsight.notificationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Mail configuration.
 * When SMTP_HOST is set, real SMTP delivery is used.
 * When absent (default / dev), a stub JavaMailSender is returned that logs
 * notifications instead of sending them — no external dependency required.
 */
@Configuration
@Slf4j
public class MailConfig {

    @Value("${spring.mail.host:}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private int smtpPort;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Value("${spring.mail.password:}")
    private String smtpPassword;

    @Bean
    public JavaMailSender javaMailSender() {
        if (smtpHost == null || smtpHost.isBlank()) {
            log.info("[MailConfig] SMTP_HOST not set — using stub mail sender (log-only mode)");
            return new StubMailSender();
        }

        log.info("[MailConfig] Configuring real SMTP sender → {}:{}", smtpHost, smtpPort);
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtpHost);
        sender.setPort(smtpPort);
        sender.setUsername(smtpUsername);
        sender.setPassword(smtpPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");

        return sender;
    }
}
