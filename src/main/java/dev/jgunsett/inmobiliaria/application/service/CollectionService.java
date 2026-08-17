package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.collection.OverdueCollectionResponse;
import dev.jgunsett.inmobiliaria.application.dto.collection.OverdueInvoiceResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.repository.InvoiceRepository;
import dev.jgunsett.inmobiliaria.repository.PayRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionService {

    private final InvoiceRepository invoiceRepository;
    private final PayRepository payRepository;
    private final LateFeeService lateFeeService;

    public OverdueCollectionResponse getOverdueInvoices(int page, int size, int minDays, Integer maxDays) {
        if (page < 0 || size < 1) {
            throw new BusinessException("La página y el tamaño deben ser válidos");
        }
        if (maxDays != null && maxDays < minDays) {
            throw new BusinessException("El máximo de días no puede ser menor al mínimo");
        }

        LocalDate today = LocalDate.now();
        lateFeeService.refreshOverdueInvoices(today);
        List<OverdueInvoiceResponse> overdue = invoiceRepository
                .findByTypeAndStatusInAndDueDateBefore(
                        InvoiceType.RENT,
                        List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID),
                        today
                )
                .stream()
                .map(invoice -> toResponse(invoice, today))
                .filter(invoice -> invoice.getOutstanding().compareTo(BigDecimal.ZERO) > 0)
                .filter(invoice -> invoice.getDaysOverdue() >= Math.max(1, minDays))
                .filter(invoice -> maxDays == null || invoice.getDaysOverdue() <= maxDays)
                .sorted(Comparator.comparingLong(OverdueInvoiceResponse::getDaysOverdue).reversed())
                .toList();

        int fromIndex = Math.min(page * size, overdue.size());
        int toIndex = Math.min(fromIndex + size, overdue.size());
        int totalPages = overdue.isEmpty() ? 0 : (int) Math.ceil((double) overdue.size() / size);

        return OverdueCollectionResponse.builder()
                .content(overdue.subList(fromIndex, toIndex))
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .totalElements(overdue.size())
                .totalOutstanding(overdue.stream()
                        .map(OverdueInvoiceResponse::getOutstanding)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .oldestDaysOverdue(overdue.stream()
                        .mapToLong(OverdueInvoiceResponse::getDaysOverdue)
                        .max()
                        .orElse(0))
                .build();
    }

    public int refreshLateFees() {
        return lateFeeService.refreshOverdueInvoices();
    }

    private OverdueInvoiceResponse toResponse(Invoice invoice, LocalDate today) {
        BigDecimal paid = payRepository.sumAmountByInvoiceId(invoice.getId());
        if (paid == null) paid = BigDecimal.ZERO;

        BigDecimal total = invoice.getTotal() == null ? BigDecimal.ZERO : invoice.getTotal();
        return OverdueInvoiceResponse.builder()
                .invoiceId(invoice.getId())
                .code(invoice.getCode())
                .customerId(invoice.getCustomer().getId())
                .customerName(invoice.getCustomer().getFullName())
                .customerEmail(invoice.getCustomer().getEmail())
                .customerPhone(invoice.getCustomer().getPhone())
                .contractId(invoice.getContract() == null ? null : invoice.getContract().getId())
                .propertyName(invoice.getContract() == null || invoice.getContract().getProperty() == null
                        ? null
                        : invoice.getContract().getProperty().getName())
                .dueDate(invoice.getDueDate())
                .daysOverdue(ChronoUnit.DAYS.between(invoice.getDueDate(), today))
                .total(total)
                .paid(paid)
                .outstanding(total.subtract(paid))
                .build();
    }
}
