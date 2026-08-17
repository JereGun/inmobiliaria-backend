package dev.jgunsett.inmobiliaria.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final SystemSettingService systemSettingService;

    public EmailSendResult sendNotificationEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) return EmailSendResult.failed("El destinatario está vacío");

        return send(toEmail, subject, body, null, null, false);
    }

    public EmailSendResult sendEmailWithAttachment(
            String toEmail,
            String subject,
            String body,
            String attachmentName,
            byte[] attachment
    ) {
        if (toEmail == null || toEmail.isBlank()) return EmailSendResult.failed("El destinatario está vacío");
        return send(toEmail, subject, body, attachmentName, attachment, false);
    }

    public EmailSendResult sendHtmlEmailWithAttachment(
            String toEmail,
            String subject,
            String htmlBody,
            String attachmentName,
            byte[] attachment
    ) {
        if (toEmail == null || toEmail.isBlank()) return EmailSendResult.failed("El destinatario está vacío");
        return send(toEmail, subject, htmlBody, attachmentName, attachment, true);
    }

    private EmailSendResult send(
            String toEmail,
            String subject,
            String body,
            String attachmentName,
            byte[] attachment,
            boolean html
    ) {

        try {
            String enabled = readSetting("email.enabled");
            if (!"true".equalsIgnoreCase(enabled)) {
                return EmailSendResult.skipped("El envío de emails está deshabilitado");
            }

            String host = readSetting("email.smtp.host");
            String portStr = readSetting("email.smtp.port");
            String username = readSetting("email.smtp.username");
            String password = readSetting("email.smtp.password");
            String from = readSetting("email.smtp.from");

            if (host == null || host.isBlank()) {
                log.warn("Email: smtp.host no configurado, se omite el envío");
                return EmailSendResult.failed("SMTP no configurado");
            }

            JavaMailSenderImpl sender = createSender(host, portStr, username, password);
            String fromAddress = from != null && !from.isBlank() ? from : username;

            if (attachment != null && attachmentName != null && !attachmentName.isBlank()) {
                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(body, html);
                helper.addAttachment(attachmentName, () -> new java.io.ByteArrayInputStream(attachment));
                sender.send(message);
            } else {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                sender.send(message);
            }
            log.info("Email enviado a {}: {}", toEmail, subject);
            return EmailSendResult.sent();
        } catch (Exception ex) {
            log.warn("No se pudo enviar el email a {}: {}", toEmail, ex.getMessage());
            return EmailSendResult.failed(ex.getMessage() == null ? "Error SMTP" : ex.getMessage());
        }
    }

    private JavaMailSenderImpl createSender(String host, String portStr, String username, String password) {
        int port = 587;
        try { port = Integer.parseInt(portStr); } catch (Exception ignored) {}

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return sender;
    }

    private String readSetting(String key) {
        try {
            return systemSettingService.findEntityByKey(key).getValue();
        } catch (Exception ex) {
            return null;
        }
    }
}
