package dev.jgunsett.inmobiliaria.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceResponse;
import dev.jgunsett.inmobiliaria.application.dto.invoice.InvoiceUpdateRequest;
import dev.jgunsett.inmobiliaria.application.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Validated
public class InvoiceController {

    private final InvoiceService invoiceService;

    // Crear Invoice
    @PostMapping
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody InvoiceCreateRequest request) {

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

    // Listar Invoices (paginado)
    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<InvoiceResponse> response = invoiceService.getAll(page, size);

        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/{id}/issue")
    public ResponseEntity<InvoiceResponse> issue(
            @PathVariable Long id) {

        InvoiceResponse response = invoiceService.issue(id);

        return ResponseEntity.ok(response);
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