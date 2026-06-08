package dev.jgunsett.inmobiliaria.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final SystemSettingService systemSettingService;

    public void sendNotificationEmail(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isBlank()) return;

        try {
            String enabled = readSetting("email.enabled");
            if (!"true".equalsIgnoreCase(enabled)) return;

            String host = readSetting("email.smtp.host");
            String portStr = readSetting("email.smtp.port");
            String username = readSetting("email.smtp.username");
            String password = readSetting("email.smtp.password");
            String from = readSetting("email.smtp.from");

            if (host == null || host.isBlank()) {
                log.warn("Email: smtp.host no configurado, se omite el envío");
                return;
            }

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

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from != null && !from.isBlank() ? from : username);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            sender.send(message);
            log.info("Email enviado a {}: {}", toEmail, subject);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el email a {}: {}", toEmail, ex.getMessage());
        }
    }

    private String readSetting(String key) {
        try {
            return systemSettingService.findEntityByKey(key).getValue();
        } catch (Exception ex) {
            return null;
        }
    }
}
