package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.InvoiceDeliveryStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceDeliveryResponse {

    private Long id;
    private Long invoiceId;
    private String channel;
    private String recipientEmail;
    private InvoiceDeliveryStatus status;
    private Integer attempts;
    private LocalDateTime sentAt;
    private LocalDateTime lastAttemptAt;
    private String lastError;
    private LocalDateTime creationDate;
}
