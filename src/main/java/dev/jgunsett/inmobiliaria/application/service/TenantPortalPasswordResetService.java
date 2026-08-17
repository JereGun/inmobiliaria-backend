package dev.jgunsett.inmobiliaria.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalForgotPasswordRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalResetPasswordRequest;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalPasswordResetToken;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAuditEventType;
import dev.jgunsett.inmobiliaria.exception.UnauthorizedException;
import dev.jgunsett.inmobiliaria.repository.TenantPortalAccountRepository;
import dev.jgunsett.inmobiliaria.repository.TenantPortalPasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantPortalPasswordResetService {

    private static final int TOKEN_VALIDITY_MINUTES = 30;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TenantPortalAccountRepository accountRepository;
    private final TenantPortalPasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;
    private final TenantPortalAuditService auditService;

    @Value("${app.portal.url}")
    private String portalUrl;

    /** Always completes without revealing whether an account exists. */
    public void requestReset(TenantPortalForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        TenantPortalAccount account = accountRepository.findByEmailIgnoreCase(email).orElse(null);
        if (account == null || account.getStatus() != TenantPortalAccountStatus.ACTIVE) {
            log.info("Recuperación de portal solicitada para cuenta inexistente o inactiva");
            return;
        }

        tokenRepository.findByAccountIdAndUsedAtIsNull(account.getId())
                .forEach(token -> token.setUsedAt(LocalDateTime.now()));

        String rawToken = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);
        tokenRepository.save(TenantPortalPasswordResetToken.builder()
                .account(account)
                .tokenHash(hash(rawToken))
                .expiresAt(expiresAt)
                .build());

        String resetUrl = portalUrl.replaceAll("/+$", "") + "/restablecer-clave?token=" + rawToken;
        emailSenderService.sendNotificationEmail(
                account.getEmail(),
                "Restablecé tu contraseña del Portal de Inquilinos",
                "Recibimos una solicitud para restablecer tu contraseña.\n\n"
                        + "Usá este enlace para elegir una nueva contraseña:\n" + resetUrl + "\n\n"
                        + "El enlace vence en " + TOKEN_VALIDITY_MINUTES + " minutos. Si no hiciste esta solicitud, podés ignorar este correo."
        );
        auditService.record(account, TenantPortalAuditEventType.PASSWORD_RESET_REQUESTED, "Recuperación de contraseña solicitada");
    }

    public void reset(TenantPortalResetPasswordRequest request) {
        TenantPortalPasswordResetToken token = tokenRepository
                .findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(hash(request.getToken()), LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("El enlace de recuperación es inválido, ya fue utilizado o venció"));

        TenantPortalAccount account = token.getAccount();
        if (account.getStatus() != TenantPortalAccountStatus.ACTIVE) {
            throw new UnauthorizedException("La cuenta del portal no está activa");
        }
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        token.setUsedAt(LocalDateTime.now());
        auditService.record(account, TenantPortalAuditEventType.PASSWORD_RESET_COMPLETED, "Contraseña restablecida");
        log.info("Contraseña del portal restablecida para la cuenta {}", account.getId());
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
            throw new java.lang.IllegalStateException("No se pudo preparar el token de recuperación", ex);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
