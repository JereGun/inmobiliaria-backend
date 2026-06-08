package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.dashboard.DashboardSummaryResponse;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PropertyRepository propertyRepository;
    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDate in60Days = today.plusDays(60);

        long totalProperties = propertyRepository.count();
        long totalCustomers = customerRepository.count();
        long totalContracts = contractRepository.count();
        long activeContracts = contractRepository.countByStatus(ContractStatus.ACTIVE);

        BigDecimal activeRentTotal = contractRepository.sumBaseRentalAmountByStatus(ContractStatus.ACTIVE);

        long issuedInvoicesCount = invoiceRepository.countByStatus(InvoiceStatus.ISSUED);
        BigDecimal issuedInvoicesTotal = invoiceRepository.sumTotalByStatus(InvoiceStatus.ISSUED);

        long contractsExpiringIn60Days = contractRepository.countByStatusAndEndDateBetween(
                ContractStatus.ACTIVE, today, in60Days);

        return new DashboardSummaryResponse(
                totalProperties,
                totalCustomers,
                totalContracts,
                activeContracts,
                activeRentTotal != null ? activeRentTotal : BigDecimal.ZERO,
                issuedInvoicesCount,
                issuedInvoicesTotal != null ? issuedInvoicesTotal : BigDecimal.ZERO,
                contractsExpiringIn60Days
        );
    }
}
