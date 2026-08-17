package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.pay.PayCreateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.Pay;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.domain.enums.PayMedium;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.NotificationRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;

@ExtendWith(MockitoExtension.class)
class PayServiceTest {

    @Mock private PayRepository payRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private LateFeeService lateFeeService;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void createSavesPaymentLinkedToInvoice() {
        Invoice invoice = invoice();
        Pay saved = Pay.builder()
                .id(1L)
                .amount(new BigDecimal("50000"))
                .date(LocalDate.now())
                .medium(PayMedium.BANK_TRANSFER)
                .invoice(invoice)
                .build();

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(payRepository.sumAmountByInvoiceId(10L)).thenReturn(BigDecimal.ZERO);
        when(payRepository.save(any())).thenReturn(saved);

        var response = service().create(createRequest());

        ArgumentCaptor<Pay> captor = ArgumentCaptor.forClass(Pay.class);
        verify(payRepository).save(captor.capture());

        assertThat(captor.getValue().getInvoice().getId()).isEqualTo(10L);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("50000");
        assertThat(captor.getValue().getMedium()).isEqualTo(PayMedium.BANK_TRANSFER);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    void createRejectsWhenInvoiceNotFound() {
        when(invoiceRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(createRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createMarksInvoiceAsPaidWhenPaymentCoversOutstandingBalance() {
        Invoice invoice = invoice();
        invoice.setTotal(new BigDecimal("50000"));
        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(payRepository.sumAmountByInvoiceId(10L)).thenReturn(BigDecimal.ZERO);
        when(payRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationRepository.findByInvoiceIdAndTypeAndReadFalse(any(), any())).thenReturn(java.util.List.of());

        service().create(createRequest());

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void createRejectsPaymentAboveOutstandingBalance() {
        Invoice invoice = invoice();
        invoice.setTotal(new BigDecimal("50000"));
        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(payRepository.sumAmountByInvoiceId(10L)).thenReturn(BigDecimal.ZERO);

        PayCreateRequest request = createRequest();
        request.setAmount(new BigDecimal("50001"));

        assertThatThrownBy(() -> service().create(request))
                .isInstanceOf(dev.jgunsett.inmobiliaria.exception.BusinessException.class)
                .hasMessageContaining("saldo pendiente");
    }

    // -------------------------------------------------------------------------
    // findById()
    // -------------------------------------------------------------------------

    @Test
    void findByIdRejectsWhenPaymentNotFound() {
        when(payRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PayService service() {
        return new PayService(payRepository, invoiceRepository, notificationRepository, lateFeeService);
    }

    private Invoice invoice() {
        return Invoice.builder()
                .id(10L)
                .type(InvoiceType.RENT)
                .status(InvoiceStatus.ISSUED)
                .total(new BigDecimal("100000"))
                .build();
    }

    private PayCreateRequest createRequest() {
        PayCreateRequest req = new PayCreateRequest();
        req.setInvoiceId(10L);
        req.setAmount(new BigDecimal("50000"));
        req.setDate(LocalDate.now());
        req.setMedium(PayMedium.BANK_TRANSFER);
        return req;
    }
}
