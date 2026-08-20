package dev.jgunsett.inmobiliaria.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppApiService {

    private final SystemSettingService systemSettingService;
    private final RestClient restClient = RestClient.builder().baseUrl("https://graph.facebook.com").build();

    public WhatsAppSendResult sendTemplate(String phone, String templateKey, List<String> bodyParameters) {
        if (!isEnabled()) return WhatsAppSendResult.skipped("El envío por WhatsApp está deshabilitado");
        if (phone == null || phone.isBlank()) return WhatsAppSendResult.skipped("El cliente no tiene teléfono de WhatsApp");

        String version = read("whatsapp.api.version");
        String phoneNumberId = read("whatsapp.phone-number-id");
        String accessToken = read("whatsapp.access-token");
        String templateName = read(templateKey);
        String languageCode = defaultIfBlank(read("whatsapp.language-code"), "es_AR");

        if (isBlank(version) || isBlank(phoneNumberId) || isBlank(accessToken) || isBlank(templateName)) {
            return WhatsAppSendResult.skipped("La configuración de la API o de la plantilla de WhatsApp está incompleta");
        }

        List<Map<String, String>> parameters = bodyParameters.stream()
                .map(value -> Map.of("type", "text", "text", value == null ? "" : value))
                .toList();

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizePhone(phone),
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", languageCode),
                        "components", List.of(Map.of("type", "body", "parameters", parameters))
                )
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri("/{version}/{phoneNumberId}/messages", version, phoneNumberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            String messageId = extractMessageId(response);
            return WhatsAppSendResult.sent(messageId);
        } catch (RestClientResponseException ex) {
            log.warn("WhatsApp rechazó el mensaje: status={}, body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return WhatsAppSendResult.failed("La API de WhatsApp rechazó el mensaje (HTTP " + ex.getStatusCode().value() + ")");
        } catch (Exception ex) {
            log.warn("No se pudo enviar el mensaje por WhatsApp: {}", ex.getMessage());
            return WhatsAppSendResult.failed(ex.getMessage() == null ? "Error al comunicarse con WhatsApp" : ex.getMessage());
        }
    }

    private String extractMessageId(Map<?, ?> response) {
        if (response == null || !(response.get("messages") instanceof List<?> messages) || messages.isEmpty()) return null;
        Object first = messages.get(0);
        return first instanceof Map<?, ?> map && map.get("id") != null ? String.valueOf(map.get("id")) : null;
    }

    private boolean isEnabled() {
        return "true".equalsIgnoreCase(read("whatsapp.enabled"));
    }

    private String read(String key) {
        try {
            return systemSettingService.findEntityByKey(key).getValue();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String normalizePhone(String value) {
        return value.replaceAll("[^0-9]", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
