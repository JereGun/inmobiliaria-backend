package dev.jgunsett.inmobiliaria.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.pay.PayResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalDashboardResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalInvoiceResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalProfileResponse;
import dev.jgunsett.inmobiliaria.application.service.TenantPortalAccountService;
import dev.jgunsett.inmobiliaria.application.service.TenantPortalInvoiceService;
import dev.jgunsett.inmobiliaria.security.TenantPortalPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-portal")
@RequiredArgsConstructor
public class TenantPortalController {

    private final TenantPortalAccountService accountService;
    private final TenantPortalInvoiceService invoiceService;

    @GetMapping("/me")
    public ResponseEntity<TenantPortalProfileResponse> me(@AuthenticationPrincipal TenantPortalPrincipal principal) {
        return ResponseEntity.ok(accountService.profile(principal.accountId()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<TenantPortalDashboardResponse> dashboard(@AuthenticationPrincipal TenantPortalPrincipal principal) {
        return ResponseEntity.ok(invoiceService.dashboard(principal.customerId()));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<TenantPortalInvoiceResponse>> invoices(
            @AuthenticationPrincipal TenantPortalPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(invoiceService.invoices(principal.customerId(), page, Math.min(size, 50)));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<TenantPortalInvoiceResponse> invoice(
            @AuthenticationPrincipal TenantPortalPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.invoice(principal.customerId(), id));
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<ByteArrayResource> invoicePdf(
            @AuthenticationPrincipal TenantPortalPrincipal principal,
            @PathVariable Long id) {
        byte[] pdf = invoiceService.invoicePdf(principal.customerId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".pdf\"")
                .body(new ByteArrayResource(pdf));
    }

    @GetMapping("/invoices/{id}/payments")
    public ResponseEntity<List<PayResponse>> payments(
            @AuthenticationPrincipal TenantPortalPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.payments(principal.customerId(), id));
    }
}
