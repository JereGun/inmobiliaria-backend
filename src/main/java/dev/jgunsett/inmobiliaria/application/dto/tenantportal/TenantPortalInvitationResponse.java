package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalInvitationResponse {
    Long accountId;
    Long customerId;
    String email;
    LocalDateTime expiresAt;
    boolean emailSent;
    String deliveryMessage;
}
