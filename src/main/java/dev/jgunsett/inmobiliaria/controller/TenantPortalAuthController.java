package dev.jgunsett.inmobiliaria.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalActivationRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAuthResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalLoginRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalForgotPasswordRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalResetPasswordRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalRefreshRequest;
import dev.jgunsett.inmobiliaria.application.service.TenantPortalAccountService;
import dev.jgunsett.inmobiliaria.application.service.TenantPortalPasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tenant-portal/auth")
@Validated
@RequiredArgsConstructor
public class TenantPortalAuthController {

    private final TenantPortalAccountService accountService;
    private final TenantPortalPasswordResetService passwordResetService;

    @PostMapping("/activate")
    public ResponseEntity<TenantPortalAuthResponse> activate(@Valid @RequestBody TenantPortalActivationRequest request) {
        return ResponseEntity.ok(accountService.activate(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TenantPortalAuthResponse> login(@Valid @RequestBody TenantPortalLoginRequest request) {
        return ResponseEntity.ok(accountService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TenantPortalAuthResponse> refresh(@Valid @RequestBody TenantPortalRefreshRequest request) {
        return ResponseEntity.ok(accountService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody TenantPortalRefreshRequest request) {
        accountService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(
            @Valid @RequestBody TenantPortalForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(java.util.Map.of("message", "Si el email está registrado, recibirás un enlace para restablecer tu contraseña"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @Valid @RequestBody TenantPortalResetPasswordRequest request) {
        passwordResetService.reset(request);
        return ResponseEntity.ok(java.util.Map.of("message", "Contraseña restablecida correctamente"));
    }
}
