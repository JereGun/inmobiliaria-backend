package dev.jgunsett.inmobiliaria.application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLineRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLineResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.InvoiceLine;

@Component
public class InvoiceMapper {

    // CreateRequest → Invoice (sin relaciones ni estado)
    public Invoice toEntity(InvoiceCreateRequest request) {

        return Invoice.builder()
                .date(request.getDate())
                .type(request.getType())
                .build();
    }

    // LineRequest → InvoiceLine
    public InvoiceLine toLineEntity(InvoiceLineRequest request, Invoice invoice) {

        return InvoiceLine.builder()
                .invoice(invoice)
                .concept(request.getConcept())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();
    }

    // Invoice → Response
    public InvoiceResponse toResponse(Invoice invoice) {

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .code(invoice.getCode())
                .type(invoice.getType())
                .status(invoice.getStatus())
                .date(invoice.getDate())
                .total(invoice.getTotal())
                .customerId(invoice.getCustomer().getId())
                .contractId(
                        invoice.getContract() != null
                                ? invoice.getContract().getId()
                                : null
                )
                .lines(
                        invoice.getLines()
                                .stream()
                                .map(this::toLineResponse)
                                .toList()
                )
                .creationDate(invoice.getCreationDate())
                .modificationDate(invoice.getModificationDate())
                .build();
    }

    // InvoiceLine → Response
    private InvoiceLineResponse toLineResponse(InvoiceLine line) {

        return InvoiceLineResponse.builder()
                .id(line.getId())
                .concept(line.getConcept())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .subtotal(line.getSubtotal())
                .build();
    }
}