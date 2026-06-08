package dev.jgunsett.inmobiliaria.application.dto.dashboard;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalProperties,
        long totalCustomers,
        long totalContracts,
        long activeContracts,
        BigDecimal activeRentTotal,
        long issuedInvoicesCount,
        BigDecimal issuedInvoicesTotal,
        long contractsExpiringIn60Days
) {}
