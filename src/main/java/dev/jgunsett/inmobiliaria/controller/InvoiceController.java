package dev.jgunsett.inmobiliaria.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceBatchRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceBatchResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceDeliveryResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceLateFeeRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.WhatsAppDeliveryResponse;
import dev.jgunsett.inmobiliaria.application.service.InvoiceService;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    // Crear Invoice
    @PostMapping
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody InvoiceCreateRequest request,
            Principal principal) {

        log.info("Solicitud de creación de factura recibida para {}", principal.getName());

        InvoiceResponse response = invoiceService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
	 // Actualizar Invoice (solo DRAFT)
	
	 @PutMapping("/{id}") 
	 public ResponseEntity<InvoiceResponse> update(
	         @PathVariable Long id,
	         @Valid @RequestBody InvoiceUpdateRequest request) {
	
	     InvoiceResponse response = invoiceService.update(id, request);
	
	     return ResponseEntity.ok(response);
	 }

    // Buscar Invoice por ID
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(
            @PathVariable Long id) {

        InvoiceResponse response = invoiceService.getById(id);

        return ResponseEntity.ok(response);
    }

    // Listar Invoices (paginado), con filtro de fecha opcional
    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Page<InvoiceResponse> response = (from != null && to != null)
                ? invoiceService.getAllByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59), page, size)
                : invoiceService.getAll(page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Page<InvoiceResponse>> getByStatus(
            @RequestParam InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Page<InvoiceResponse> response = (from != null && to != null)
                ? invoiceService.getByStatusAndDateRange(status, from.atStartOfDay(), to.atTime(23, 59, 59), page, size)
                : invoiceService.getByStatus(status, page, size);

        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/{id}/issue")
    public ResponseEntity<InvoiceResponse> issue(
            @PathVariable Long id) {

        InvoiceResponse response = invoiceService.issue(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/revert-to-draft")
    public ResponseEntity<InvoiceResponse> revertToDraft(
            @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.revertToDraft(id));
    }

    @PostMapping("/batch/issue")
    public ResponseEntity<InvoiceBatchResponse> issueBatch(
            @Valid @RequestBody InvoiceBatchRequest request) {
        return ResponseEntity.ok(invoiceService.issueBatch(request.getInvoiceIds()));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<InvoiceDeliveryResponse> send(
            @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.send(id));
    }

    @PostMapping("/batch/send")
    public ResponseEntity<InvoiceBatchResponse> sendBatch(
            @Valid @RequestBody InvoiceBatchRequest request) {
        return ResponseEntity.ok(invoiceService.sendBatch(request.getInvoiceIds()));
    }

    @PostMapping("/{id}/send-whatsapp")
    public ResponseEntity<WhatsAppDeliveryResponse> sendWhatsApp(
            @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.sendWhatsApp(id));
    }

    @PostMapping("/batch/send-whatsapp")
    public ResponseEntity<InvoiceBatchResponse> sendWhatsAppBatch(
            @Valid @RequestBody InvoiceBatchRequest request) {
        return ResponseEntity.ok(invoiceService.sendWhatsAppBatch(request.getInvoiceIds()));
    }

    @PatchMapping("/{id}/late-fee")
    public ResponseEntity<InvoiceResponse> applyLateFeeManually(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceLateFeeRequest request) {
        return ResponseEntity.ok(invoiceService.applyLateFeeManually(id, request));
    }

    @GetMapping("/{id}/deliveries")
    public ResponseEntity<List<InvoiceDeliveryResponse>> getDeliveries(
            @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getDeliveries(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> pay(
            @PathVariable Long id) {

        InvoiceResponse response = invoiceService.pay(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponse> cancel(
            @PathVariable Long id) {

        InvoiceResponse response = invoiceService.cancel(id);

        return ResponseEntity.ok(response);
    }

}
