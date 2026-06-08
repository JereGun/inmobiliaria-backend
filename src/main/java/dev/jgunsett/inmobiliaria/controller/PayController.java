package dev.jgunsett.inmobiliaria.controller;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import dev.jgunsett.inmobiliaria.application.dto.pay.PayCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.pay.PayResponse;
import dev.jgunsett.inmobiliaria.application.service.PayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pays")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PayResponse create(@Valid @RequestBody PayCreateRequest request) {
        return payService.create(request);
    }

    @GetMapping
    public Page<PayResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return (from != null && to != null)
                ? payService.findAllByDateRange(from, to, page, size)
                : payService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public PayResponse findById(@PathVariable Long id) {
        return payService.findById(id);
    }

    @GetMapping("/invoice/{invoiceId}")
    public List<PayResponse> findByInvoice(@PathVariable Long invoiceId) {
        return payService.findByInvoice(invoiceId);
    }
}
