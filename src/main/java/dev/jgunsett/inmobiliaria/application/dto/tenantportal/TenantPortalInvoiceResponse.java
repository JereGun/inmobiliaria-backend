package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLineResponse;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalInvoiceResponse {
    Long id;
    String code;
    InvoiceStatus status;
    LocalDate dueDate;
    String billingPeriod;
    BigDecimal total;
    BigDecimal paidAmount;
    BigDecimal outstandingAmount;
    BigDecimal lateFeeAmount;
    boolean overdue;
    List<InvoiceLineResponse> lines;
}
