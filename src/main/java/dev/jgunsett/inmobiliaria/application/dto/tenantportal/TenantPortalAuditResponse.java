package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAuditEventType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalAuditResponse {
    Long id;
    TenantPortalAuditEventType eventType;
    String detail;
    LocalDateTime occurredAt;
}
