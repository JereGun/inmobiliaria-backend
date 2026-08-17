package dev.jgunsett.inmobiliaria.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class TenantPortalJwtService {

    private static final String TOKEN_SCOPE = "TENANT_PORTAL";
    private final SecretKey signingKey;
    private final long expiration;

    public TenantPortalJwtService(
            @Value("${app.security.portal-jwt.secret}") String secret,
            @Value("${app.security.portal-jwt.expiration}") long expiration) {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("PORTAL_JWT_SECRET debe tener al menos 32 bytes.");
        }
        if (expiration <= 0) throw new IllegalStateException("PORTAL_JWT_EXPIRATION debe ser mayor que cero.");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(TenantPortalAccount account) {
        Date now = new Date();
        return Jwts.builder()
                .claims(Map.of(
                        "accountId", account.getId(),
                        "customerId", account.getCustomer().getId(),
                        "scope", TOKEN_SCOPE
                ))
                .subject(account.getEmail())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(signingKey)
                .compact();
    }

    public Long extractAccountId(String token) {
        return extractAllClaims(token).get("accountId", Long.class);
    }

    public boolean isTenantPortalToken(String token) {
        return TOKEN_SCOPE.equals(extractAllClaims(token).get("scope", String.class));
    }

    public long getExpiration() { return expiration; }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
