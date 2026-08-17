package dev.jgunsett.inmobiliaria.application.service;

public record EmailSendResult(boolean success, boolean skipped, String message) {

    public static EmailSendResult sent() {
        return new EmailSendResult(true, false, "Enviado");
    }

    public static EmailSendResult skipped(String message) {
        return new EmailSendResult(false, true, message);
    }

    public static EmailSendResult failed(String message) {
        return new EmailSendResult(false, false, message);
    }
}
