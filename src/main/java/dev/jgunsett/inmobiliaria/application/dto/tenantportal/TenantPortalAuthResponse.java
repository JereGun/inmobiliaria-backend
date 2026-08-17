package dev.jgunsett.inmobiliaria.application.dto.tenantportal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TenantPortalAuthResponse {
    String accessToken;
    long expiresIn;
    String refreshToken;
    long refreshExpiresIn;
    TenantPortalProfileResponse profile;
}
