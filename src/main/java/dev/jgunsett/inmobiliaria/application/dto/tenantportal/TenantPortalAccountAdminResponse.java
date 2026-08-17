package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalAccountAdminResponse {
    Long accountId;
    Long customerId;
    String email;
    TenantPortalAccountStatus status;
    LocalDateTime activatedAt;
    LocalDateTime lastLoginAt;
}
