package dev.jgunsett.inmobiliaria.application.dto.collection;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverdueInvoiceResponse {
    private Long invoiceId;
    private String code;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Long contractId;
    private String propertyName;
    private LocalDate dueDate;
    private long daysOverdue;
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal outstanding;
}
