package dev.jgunsett.inmobiliaria.application.dto.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WhatsAppDeliveryResponse {
    private Long invoiceId;
    private String status;
    private boolean success;
    private String message;
    private String providerMessageId;
}
