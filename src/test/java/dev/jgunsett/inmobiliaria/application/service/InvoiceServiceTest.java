package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLineRequest;
import dev.jgunsett.inmobiliaria.application.mapper.InvoiceMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceLine;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private InvoiceMapper invoiceMapper;
    @Mock private NotificationRepository notificationRepository;

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Test
    void createSavesInvoiceInDraftStatus() {
        Invoice invoice = emptyInvoice();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(invoiceMapper.toEntity(any())).thenReturn(invoice);
        when(invoiceMapper.toLineEntity(any(), any())).thenReturn(
                InvoiceLine.builder().subtotal(BigDecimal.ZERO).build());
        when(invoiceRepository.save(any())).thenReturn(invoice);
        when(invoiceMapper.toResponse(any())).thenReturn(null);

        service().create(createRequest());

        verify(invoiceRepository).save(any(Invoice.class));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void createRejectsWhenCustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(createRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente");

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void createRejectsWhenContractNotFound() {
        InvoiceCreateRequest req = createRequest();
        req.setContractId(99L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(contractRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Contrato");

        verify(invoiceRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void updateRejectsNonDraftInvoice() {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service().update(5L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");

        verify(invoiceRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // issue()
    // -------------------------------------------------------------------------

    @Test
    void issueTransitionsToDraftToIssued() {
        Invoice invoice = draftInvoiceWithLine();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toResponse(any())).thenReturn(null);

        service().issue(5L);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void issueRejectsWhenNotDraft() {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service().issue(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void issueRejectsWhenInvoiceHasNoLines() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service().issue(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("lineas");
    }

    // -------------------------------------------------------------------------
    // pay()
    // -------------------------------------------------------------------------

    @Test
    void payTransitionsIssuedToPaid() {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(notificationRepository.findByInvoiceIdAndTypeAndReadFalse(any(), any())).thenReturn(List.of());
        when(invoiceMapper.toResponse(any())).thenReturn(null);

        service().pay(5L);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void payRejectsWhenNotIssued() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service().pay(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISSUED");
    }

    // -------------------------------------------------------------------------
    // cancel()
    // -------------------------------------------------------------------------

    @Test
    void cancelRejectsPaidInvoice() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service().cancel(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pagada");
    }

    @Test
    void cancelRejectsAlreadyCanceledInvoice() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.CANCELED);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> service().cancel(5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void cancelDraftInvoiceSucceeds() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toResponse(any())).thenReturn(null);

        service().cancel(5L);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELED);
    }

    @Test
    void cancelIssuedInvoiceSucceeds() {
        Invoice invoice = issuedInvoice();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toResponse(any())).thenReturn(null);

        service().cancel(5L);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELED);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private InvoiceService service() {
        return new InvoiceService(
                invoiceRepository, customerRepository, contractRepository,
                invoiceMapper, notificationRepository);
    }

    private Customer customer() {
        return Customer.builder().id(1L).name("Ana").surname("Perez").build();
    }

    private Invoice emptyInvoice() {
        Invoice invoice = Invoice.builder()
                .id(5L)
                .customer(customer())
                .type(InvoiceType.RENT)
                .date(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .build();
        invoice.setLines(new ArrayList<>());
        return invoice;
    }

    private Invoice issuedInvoice() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.ISSUED);
        return invoice;
    }

    private Invoice draftInvoiceWithLine() {
        Invoice invoice = emptyInvoice();
        invoice.setStatus(InvoiceStatus.DRAFT);
        InvoiceLine line = InvoiceLine.builder()
                .concept("Alquiler")
                .quantity(1)
                .unitPrice(new BigDecimal("50000"))
                .subtotal(new BigDecimal("50000"))
                .build();
        invoice.getLines().add(line);
        return invoice;
    }

    private InvoiceCreateRequest createRequest() {
        InvoiceCreateRequest req = new InvoiceCreateRequest();
        req.setCustomerId(1L);
        req.setType(InvoiceType.RENT);
        req.setDate(LocalDateTime.now());

        InvoiceLineRequest line = new InvoiceLineRequest();
        line.setConcept("Alquiler");
        line.setQuantity(1);
        line.setUnitPrice(new BigDecimal("50000"));
        req.setLines(List.of(line));

        return req;
    }
}
