package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;

@ExtendWith(MockitoExtension.class)
class TenantPortalInvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PayRepository payRepository;
    @Mock private LateFeeService lateFeeService;
    @Mock private InvoicePdfService invoicePdfService;

    @Test
    void invoiceDoesNotExposeAnotherTenantInvoice() {
        when(invoiceRepository.findByIdAndCustomerId(77L, 12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().invoice(12L, 77L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No se encontró");
    }

    private TenantPortalInvoiceService service() {
        return new TenantPortalInvoiceService(invoiceRepository, payRepository, lateFeeService, invoicePdfService);
    }
}
