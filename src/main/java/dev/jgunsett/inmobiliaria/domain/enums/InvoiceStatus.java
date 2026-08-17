package dev.jgunsett.inmobiliaria.domain.enums;

public enum InvoiceStatus {
    DRAFT,     // Borrador
    ISSUED,    // Emitida, no editable
    PARTIALLY_PAID, // Pago parcial registrado
    PAID,      // Pagada completamente
    CANCELED   // Anulada
}
