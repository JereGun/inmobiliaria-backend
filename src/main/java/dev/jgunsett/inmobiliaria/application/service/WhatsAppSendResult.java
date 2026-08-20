package dev.jgunsett.inmobiliaria.application.service;

public record WhatsAppSendResult(
        boolean success,
        boolean skipped,
        String message,
        String providerMessageId
) {
    public static WhatsAppSendResult sent(String providerMessageId) {
        return new WhatsAppSendResult(true, false, "Mensaje enviado", providerMessageId);
    }

    public static WhatsAppSendResult skipped(String message) {
        return new WhatsAppSendResult(false, true, message, null);
    }

    public static WhatsAppSendResult failed(String message) {
        return new WhatsAppSendResult(false, false, message, null);
    }
}
