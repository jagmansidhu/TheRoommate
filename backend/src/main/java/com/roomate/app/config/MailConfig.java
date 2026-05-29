package com.roomate.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${spring.mail.port:25}")
    private Integer port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        // If mail host is not configured, return a no-op mail sender so the
        // application can run without SMTP configured. This avoids startup
        // failures when EMAIL_* env vars are intentionally not provided.
        if (host == null || host.isEmpty()) {
            return new NoOpJavaMailSender();
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port != null ? port : 25);

        if (username != null && !username.isEmpty()) mailSender.setUsername(username);
        if (password != null && !password.isEmpty()) mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

    // Minimal no-op JavaMailSender implementation used when mail is not configured.
    private static class NoOpJavaMailSender implements JavaMailSender {
        @Override
        public void send(org.springframework.mail.SimpleMailMessage simpleMessage) {
            // no-op
        }

        @Override
        public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) {
            // no-op
        }

        @Override
        public jakarta.mail.internet.MimeMessage createMimeMessage() {
            return new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
        }

        @Override
        public jakarta.mail.internet.MimeMessage createMimeMessage(java.io.InputStream contentStream) {
            try {
                return new jakarta.mail.internet.MimeMessage(null, contentStream);
            } catch (jakarta.mail.MessagingException e) {
                // Return empty MIME message if unable to parse stream
                return new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
            }
        }

        @Override
        public void send(jakarta.mail.internet.MimeMessage mimeMessage) {
            // no-op
        }

        @Override
        public void send(jakarta.mail.internet.MimeMessage... mimeMessages) {
            // no-op
        }

        @Override
        public void send(org.springframework.mail.javamail.MimeMessagePreparator mimeMessagePreparator) {
            // no-op
        }

        @Override
        public void send(org.springframework.mail.javamail.MimeMessagePreparator... mimeMessagePreparators) {
            // no-op
        }
    }
}
