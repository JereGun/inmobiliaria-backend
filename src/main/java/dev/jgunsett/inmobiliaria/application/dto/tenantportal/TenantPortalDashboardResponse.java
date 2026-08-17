package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalDashboardResponse {
    BigDecimal outstandingAmount;
    long overdueInvoices;
    TenantPortalInvoiceResponse nextDueInvoice;
    List<TenantPortalInvoiceResponse> recentInvoices;
}
