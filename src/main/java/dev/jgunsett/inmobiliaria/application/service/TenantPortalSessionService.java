package dev.jgunsett.inmobiliaria.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalSession;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
import dev.jgunsett.inmobiliaria.exception.UnauthorizedException;
import dev.jgunsett.inmobiliaria.repository.TenantPortalSessionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantPortalSessionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TenantPortalSessionRepository sessionRepository;

    @Value("${app.security.portal-session.expiration}")
    private long expirationMillis;

    public TenantPortalSessionToken create(TenantPortalAccount account) {
        String rawToken = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMillis / 1000);
        sessionRepository.save(TenantPortalSession.builder()
                .account(account)
                .tokenHash(hash(rawToken))
                .expiresAt(expiresAt)
                .build());
        return new TenantPortalSessionToken(account, rawToken, expirationMillis / 1000);
    }

    public TenantPortalSessionToken rotate(String refreshToken) {
        TenantPortalSession session = sessionRepository
                .findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(hash(refreshToken), LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("La sesión del portal venció. Iniciá sesión nuevamente"));
        TenantPortalAccount account = session.getAccount();
        if (account.getStatus() != TenantPortalAccountStatus.ACTIVE) {
            throw new UnauthorizedException("La cuenta del portal no está activa");
        }
        session.setRevokedAt(LocalDateTime.now());
        session.setLastUsedAt(LocalDateTime.now());
        return create(account);
    }

    public TenantPortalAccount revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return null;
        return sessionRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(hash(refreshToken), LocalDateTime.now())
                .map(session -> {
                    session.setRevokedAt(LocalDateTime.now());
                    return session.getAccount();
                })
                .orElse(null);
    }

    public void revokeAll(Long accountId) {
        sessionRepository.findByAccountIdAndRevokedAtIsNull(accountId)
                .forEach(session -> session.setRevokedAt(LocalDateTime.now()));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new java.lang.IllegalStateException("No se pudo preparar la sesión del portal", ex);
        }
    }

    public record TenantPortalSessionToken(TenantPortalAccount account, String value, long expiresIn) {}
}
