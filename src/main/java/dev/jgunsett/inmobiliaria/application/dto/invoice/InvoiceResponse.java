package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceResponse {

    private Long id;

    private String code;

    private InvoiceType type;

    private InvoiceStatus status;

    private LocalDateTime date;

    private BigDecimal total;

    private Long customerId;

    private Long contractId;

    private List<InvoiceLineResponse> lines;

    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
}