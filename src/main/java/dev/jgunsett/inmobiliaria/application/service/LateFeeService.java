package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceLine;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LateFeeService {

    private final InvoiceRepository invoiceRepository;
    private final SystemSettingService systemSettingService;

    public int refreshOverdueInvoices() {
        return refreshOverdueInvoices(LocalDate.now());
    }

    public int refreshOverdueInvoices(LocalDate calculationDate) {
        if (!isAutoApplyEnabled()) {
            log.debug("Aplicación automática de intereses por mora deshabilitada");
            return 0;
        }

        List<Invoice> overdueInvoices = invoiceRepository.findByTypeAndStatusInAndDueDateBefore(
                InvoiceType.RENT,
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID),
                calculationDate
        );

        int updated = 0;
        for (Invoice invoice : overdueInvoices) {
            if (updateLateFee(invoice, calculationDate)) {
                updated++;
            }
        }
        log.info("Intereses por mora actualizados en {} facturas", updated);
        return updated;
    }

    public boolean updateLateFeeAutomatically(Invoice invoice, LocalDate calculationDate) {
        return isAutoApplyEnabled() && updateLateFee(invoice, calculationDate);
    }

    public boolean updateLateFee(Invoice invoice, LocalDate calculationDate) {
        if (!isEligible(invoice, calculationDate)) return false;

        BigDecimal rate = resolveDailyRate(invoice);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) return false;

        long overdueDays = ChronoUnit.DAYS.between(invoice.getDueDate(), calculationDate);
        if (overdueDays <= 0) return false;

        BigDecimal rentalAmount = invoice.getLines().stream()
                .filter(line -> !Boolean.TRUE.equals(line.getLateFee()))
                .map(InvoiceLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (rentalAmount.compareTo(BigDecimal.ZERO) <= 0) return false;

        BigDecimal dailyFee = rentalAmount
                .multiply(rate)
                .movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal accumulatedFee = dailyFee
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);

        InvoiceLine lateFeeLine = invoice.getLines().stream()
                .filter(line -> Boolean.TRUE.equals(line.getLateFee()))
                .findFirst()
                .orElseGet(() -> {
                    InvoiceLine line = InvoiceLine.builder()
                            .invoice(invoice)
                            .lateFee(true)
                            .quantity(1)
                            .build();
                    invoice.getLines().add(line);
                    return line;
                });

        boolean changed = lateFeeLine.getSubtotal() == null
                || lateFeeLine.getSubtotal().compareTo(accumulatedFee) != 0;
        lateFeeLine.setConcept("Interés por mora: " + overdueDays + " día(s) al "
                + rate.stripTrailingZeros().toPlainString() + "% diario");
        lateFeeLine.setUnitPrice(accumulatedFee);
        lateFeeLine.setQuantity(1);
        lateFeeLine.recalculateSubtotal();
        invoice.recalculateTotal();
        return changed;
    }

    private boolean isEligible(Invoice invoice, LocalDate calculationDate) {
        return invoice.getType() == InvoiceType.RENT
                && invoice.getDueDate() != null
                && invoice.getDueDate().isBefore(calculationDate)
                && (invoice.getStatus() == InvoiceStatus.ISSUED
                        || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID);
    }

    private BigDecimal resolveDailyRate(Invoice invoice) {
        if (invoice.getLateFeeDailyPercentage() != null) {
            return invoice.getLateFeeDailyPercentage();
        }
        if (invoice.getContract() == null) return null;

        BigDecimal contractRate = invoice.getContract().getLateFeePercentage();
        if (contractRate != null) {
            invoice.setLateFeeDailyPercentage(contractRate);
        }
        return contractRate;
    }

    private boolean isAutoApplyEnabled() {
        try {
            return "true".equalsIgnoreCase(
                    systemSettingService.findEntityByKey("invoice.late-fees.auto-apply.enabled").getValue()
            );
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
