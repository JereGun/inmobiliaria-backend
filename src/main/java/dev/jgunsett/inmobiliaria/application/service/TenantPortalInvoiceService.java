package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLineResponse;
import dev.jgunsett.inmobiliaria.application.dto.pay.PayResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalDashboardResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalInvoiceResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantPortalInvoiceService {

    private static final List<InvoiceStatus> VISIBLE_STATUSES = List.of(
            InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID, InvoiceStatus.PAID
    );
    private static final List<InvoiceStatus> PENDING_STATUSES = List.of(
            InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID
    );

    private final InvoiceRepository invoiceRepository;
    private final PayRepository payRepository;
    private final LateFeeService lateFeeService;
    private final InvoicePdfService invoicePdfService;

    public TenantPortalDashboardResponse dashboard(Long customerId) {
        LocalDate today = LocalDate.now();
        TenantPortalInvoiceResponse nextDue = invoiceRepository
                .findByCustomerIdAndStatusIn(customerId, PENDING_STATUSES,
                        PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "dueDate")))
                .stream()
                .map(this::toResponse)
                .findFirst()
                .orElse(null);

        List<TenantPortalInvoiceResponse> recent = invoiceRepository
                .findByCustomerIdAndStatusIn(customerId, VISIBLE_STATUSES,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "date")))
                .map(this::toResponse)
                .getContent();

        BigDecimal totalPending = invoiceRepository.sumTotalByCustomerIdAndStatusIn(customerId, PENDING_STATUSES);
        BigDecimal paidPending = payRepository.sumAmountByCustomerIdAndInvoiceStatusIn(customerId, PENDING_STATUSES);
        BigDecimal outstanding = totalPending.subtract(paidPending).max(BigDecimal.ZERO);

        long overdue = invoiceRepository.countByCustomerIdAndStatusInAndDueDateBefore(customerId, PENDING_STATUSES, today);
        return TenantPortalDashboardResponse.builder()
                .outstandingAmount(outstanding)
                .overdueInvoices(overdue)
                .nextDueInvoice(nextDue)
                .recentInvoices(recent)
                .build();
    }

    public Page<TenantPortalInvoiceResponse> invoices(Long customerId, int page, int size) {
        return invoiceRepository.findByCustomerIdAndStatusIn(
                customerId,
                VISIBLE_STATUSES,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
        ).map(this::toResponse);
    }

    public TenantPortalInvoiceResponse invoice(Long customerId, Long invoiceId) {
        return toResponse(findVisibleInvoice(customerId, invoiceId));
    }

    public byte[] invoicePdf(Long customerId, Long invoiceId) {
        return invoicePdfService.generate(findVisibleInvoice(customerId, invoiceId));
    }

    public List<PayResponse> payments(Long customerId, Long invoiceId) {
        findVisibleInvoice(customerId, invoiceId);
        return payRepository.findByInvoiceId(invoiceId).stream()
                .map(pay -> PayResponse.builder()
                        .id(pay.getId())
                        .amount(pay.getAmount())
                        .date(pay.getDate())
                        .medium(pay.getMedium())
                        .invoiceId(invoiceId)
                        .build())
                .toList();
    }

    private Invoice findVisibleInvoice(Long customerId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndCustomerId(invoiceId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la factura solicitada"));
        if (!VISIBLE_STATUSES.contains(invoice.getStatus())) {
            throw new ResourceNotFoundException("No se encontró la factura solicitada");
        }
        lateFeeService.updateLateFeeAutomatically(invoice, LocalDate.now());
        return invoice;
    }

    private TenantPortalInvoiceResponse toResponse(Invoice invoice) {
        lateFeeService.updateLateFeeAutomatically(invoice, LocalDate.now());
        BigDecimal paid = payRepository.sumAmountByInvoiceId(invoice.getId());
        if (paid == null) paid = BigDecimal.ZERO;
        BigDecimal total = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
        BigDecimal lateFee = invoice.getLines().stream()
                .filter(line -> Boolean.TRUE.equals(line.getLateFee()))
                .map(line -> line.getSubtotal() == null ? BigDecimal.ZERO : line.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return TenantPortalInvoiceResponse.builder()
                .id(invoice.getId())
                .code(invoice.getCode())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .billingPeriod(invoice.getBillingPeriod())
                .total(total)
                .paidAmount(paid)
                .outstandingAmount(total.subtract(paid).max(BigDecimal.ZERO))
                .lateFeeAmount(lateFee)
                .overdue(PENDING_STATUSES.contains(invoice.getStatus())
                        && invoice.getDueDate() != null
                        && invoice.getDueDate().isBefore(LocalDate.now()))
                .lines(invoice.getLines().stream().map(line -> InvoiceLineResponse.builder()
                        .id(line.getId())
                        .concept(line.getConcept())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .subtotal(line.getSubtotal())
                        .lateFee(line.getLateFee())
                        .build()).toList())
                .build();
    }
}
