package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceLine;
import dev.jgunsett.inmobiliaria.domain.entity.SystemSetting;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class LateFeeServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private SystemSettingService systemSettingService;

    @Test
    void updateLateFeeAccumulatesByDayWithoutChargingInterestOnInterest() {
        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        Invoice invoice = rentInvoice(dueDate);

        service().updateLateFee(invoice, dueDate.plusDays(2));

        assertThat(invoice.getTotal()).isEqualByComparingTo("103000.00");
        assertThat(lateFeeLines(invoice)).hasSize(1);
        assertThat(lateFeeLines(invoice).getFirst().getSubtotal()).isEqualByComparingTo("3000.00");

        service().updateLateFee(invoice, dueDate.plusDays(3));

        assertThat(invoice.getTotal()).isEqualByComparingTo("104500.00");
        assertThat(lateFeeLines(invoice)).hasSize(1);
        assertThat(lateFeeLines(invoice).getFirst().getSubtotal()).isEqualByComparingTo("4500.00");
    }

    @Test
    void updateLateFeeAutomaticallyDoesNothingWhenConfigurationIsDisabled() {
        LocalDate dueDate = LocalDate.of(2026, 8, 1);
        Invoice invoice = rentInvoice(dueDate);
        when(systemSettingService.findEntityByKey("invoice.late-fees.auto-apply.enabled"))
                .thenReturn(SystemSetting.builder().value("false").build());

        boolean updated = service().updateLateFeeAutomatically(invoice, dueDate.plusDays(2));

        assertThat(updated).isFalse();
        assertThat(invoice.getTotal()).isEqualByComparingTo("100000");
        assertThat(lateFeeLines(invoice)).isEmpty();
    }

    private LateFeeService service() {
        return new LateFeeService(invoiceRepository, systemSettingService);
    }

    private Invoice rentInvoice(LocalDate dueDate) {
        Invoice invoice = Invoice.builder()
                .id(1L)
                .type(InvoiceType.RENT)
                .status(InvoiceStatus.ISSUED)
                .dueDate(dueDate)
                .lateFeeDailyPercentage(new BigDecimal("1.50"))
                .total(new BigDecimal("100000"))
                .build();
        invoice.setLines(new ArrayList<>());

        InvoiceLine rentLine = InvoiceLine.builder()
                .invoice(invoice)
                .concept("Alquiler")
                .quantity(1)
                .unitPrice(new BigDecimal("100000"))
                .lateFee(false)
                .build();
        rentLine.recalculateSubtotal();
        invoice.getLines().add(rentLine);
        return invoice;
    }

    private java.util.List<InvoiceLine> lateFeeLines(Invoice invoice) {
        return invoice.getLines().stream().filter(line -> Boolean.TRUE.equals(line.getLateFee())).toList();
    }
}
