package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantPortalRefreshRequest {
    @NotBlank
    private String refreshToken;
}
