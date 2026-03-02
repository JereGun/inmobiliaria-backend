package dev.jgunsett.inmobiliaria.controller;
import java.util.List;

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

    @GetMapping("/invoice/{invoiceId}")
    public List<PayResponse> findByInvoice(@PathVariable Long invoiceId) {
        return payService.findByInvoice(invoiceId);
    }
}