package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalProfileResponse {
    Long accountId;
    Long customerId;
    String fullName;
    String email;
}
