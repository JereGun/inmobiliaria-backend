package dev.jgunsett.inmobiliaria.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalInvitationRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalInvitationResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAccountAdminResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAccountStatusUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAuditResponse;
import dev.jgunsett.inmobiliaria.application.service.TenantPortalAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-portal/admin")
@Validated
@RequiredArgsConstructor
public class TenantPortalAdminController {

    private final TenantPortalAccountService accountService;

    @PostMapping("/invitations")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public ResponseEntity<TenantPortalInvitationResponse> invite(
            @Valid @RequestBody TenantPortalInvitationRequest request) {
        return ResponseEntity.ok(accountService.invite(request.getCustomerId()));
    }

    @GetMapping("/accounts/customer/{customerId}")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public ResponseEntity<TenantPortalAccountAdminResponse> status(@PathVariable Long customerId) {
        return accountService.adminStatus(customerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/accounts/customer/{customerId}/status")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public ResponseEntity<TenantPortalAccountAdminResponse> updateStatus(
            @PathVariable Long customerId,
            @Valid @RequestBody TenantPortalAccountStatusUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateStatus(customerId, request));
    }

    @GetMapping("/accounts/customer/{customerId}/audit")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public ResponseEntity<java.util.List<TenantPortalAuditResponse>> audit(
            @PathVariable Long customerId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(accountService.audit(customerId, limit));
    }
}
