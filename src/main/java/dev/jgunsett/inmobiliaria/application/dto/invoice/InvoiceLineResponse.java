package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceLineResponse {

    private Long id;

    private String concept;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}