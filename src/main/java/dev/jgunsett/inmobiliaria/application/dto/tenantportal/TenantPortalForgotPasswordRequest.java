package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantPortalForgotPasswordRequest {
    @Email
    @NotBlank
    private String email;
}
