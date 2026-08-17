package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantPortalResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
