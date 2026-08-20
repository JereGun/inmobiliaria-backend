package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.pay.PayCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.pay.PayResponse;
import dev.jgunsett.inmobiliaria.application.mapper.PayMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.Pay;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.NotificationType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.NotificationRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class PayService {

    private final PayRepository payRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final LateFeeService lateFeeService;
    private final WhatsAppNotificationService whatsappNotificationService;

    // Mantiene compatibles las pruebas y consumidores que construyen el servicio
    // sin el canal opcional de WhatsApp.
    public PayService(
            PayRepository payRepository,
            InvoiceRepository invoiceRepository,
            NotificationRepository notificationRepository,
            LateFeeService lateFeeService
    ) {
        this(payRepository, invoiceRepository, notificationRepository, lateFeeService, null);
    }

    public PayResponse create(PayCreateRequest request) {

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.ISSUED
                && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID) {
            throw new BusinessException("Solo se pueden registrar pagos sobre facturas emitidas o parcialmente pagadas");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El importe del pago debe ser mayor a cero");
        }

        if (request.getDate() == null || request.getMedium() == null) {
            throw new BusinessException("La fecha y el medio del pago son obligatorios");
        }

        lateFeeService.updateLateFeeAutomatically(invoice, request.getDate());

        if (invoice.getTotal() == null || invoice.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La factura no tiene un total válido");
        }

        BigDecimal paidBefore = payRepository.sumAmountByInvoiceId(invoice.getId());
        if (paidBefore == null) {
            paidBefore = BigDecimal.ZERO;
        }

        BigDecimal outstanding = invoice.getTotal().subtract(paidBefore);
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new BusinessException("El pago supera el saldo pendiente de la factura");
        }

        Pay pay = Pay.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .medium(request.getMedium())
                .invoice(invoice)
                .build();

        Pay saved = payRepository.save(pay);
        BigDecimal paidAfter = paidBefore.add(request.getAmount());
        invoice.setStatus(paidAfter.compareTo(invoice.getTotal()) >= 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            notificationRepository.findByInvoiceIdAndTypeAndReadFalse(
                            invoice.getId(), NotificationType.RENT_OVERDUE)
                    .forEach(notification -> {
                        notification.setRead(true);
                        notification.setReadAt(java.time.LocalDateTime.now());
                    });
        }

        if (whatsappNotificationService != null) {
            whatsappNotificationService.sendPayment(saved);
        }

        return PayMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PayResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return payRepository.findAll(pageable)
                .map(PayMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PayResponse> findAllByDateRange(LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return payRepository.findByDateBetween(from, to, pageable)
                .map(PayMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PayResponse findById(Long id) {
        Pay pay = payRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return PayMapper.toResponse(pay);
    }

    @Transactional(readOnly = true)
    public List<PayResponse> findByInvoice(Long invoiceId) {
        return payRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(PayMapper::toResponse)
                .toList();
    }
}
