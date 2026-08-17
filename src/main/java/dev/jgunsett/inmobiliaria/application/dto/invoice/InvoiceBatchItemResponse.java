package dev.jgunsett.inmobiliaria.application.dto.invoice;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceBatchItemResponse {

    private Long invoiceId;
    private String code;
    private boolean success;
    private String message;
}
