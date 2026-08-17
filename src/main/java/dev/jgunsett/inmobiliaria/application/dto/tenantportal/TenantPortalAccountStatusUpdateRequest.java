package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TenantPortalAccountStatusUpdateRequest {
    @NotNull
    private Boolean enabled;
}
