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

import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalActivationRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAuthResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAccountAdminResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAccountStatusUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalInvitationResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalLoginRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalProfileResponse;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalRefreshRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalInvitation;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAuditEventType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.exception.UnauthorizedException;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.TenantPortalAccountRepository;
import dev.jgunsett.inmobiliaria.repository.TenantPortalInvitationRepository;
import dev.jgunsett.inmobiliaria.security.TenantPortalJwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantPortalAccountService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int INVITATION_VALIDITY_HOURS = 72;

    private final CustomerRepository customerRepository;
    private final TenantPortalAccountRepository accountRepository;
    private final TenantPortalInvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantPortalJwtService jwtService;
    private final EmailSenderService emailSenderService;
    private final TenantPortalSessionService sessionService;
    private final TenantPortalAuditService auditService;

    @Value("${app.portal.url}")
    private String portalUrl;

    public TenantPortalInvitationResponse invite(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró Cliente con el ID: " + customerId));
        String email = normalizeEmail(customer.getEmail());
        if (email == null) throw new BusinessException("El inquilino debe tener un email para habilitar el portal");

        TenantPortalAccount account = accountRepository.findByCustomerId(customerId)
                .orElseGet(() -> accountRepository.save(TenantPortalAccount.builder()
                        .customer(customer)
                        .email(email)
                        .status(TenantPortalAccountStatus.PENDING)
                        .build()));

        if (account.getStatus() == TenantPortalAccountStatus.DISABLED) {
            throw new BusinessException("La cuenta del portal está deshabilitada");
        }
        if (!account.getEmail().equals(email)) account.setEmail(email);

        invitationRepository.findByAccountIdAndConsumedAtIsNullAndRevokedAtIsNull(account.getId())
                .forEach(invitation -> invitation.setRevokedAt(LocalDateTime.now()));

        String token = generateRawToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(INVITATION_VALIDITY_HOURS);
        invitationRepository.save(TenantPortalInvitation.builder()
                .account(account)
                .tokenHash(hash(token))
                .expiresAt(expiresAt)
                .build());

        String activationUrl = portalUrl.replaceAll("/+$", "") + "/activar?token=" + token;
        EmailSendResult delivery = emailSenderService.sendNotificationEmail(
                account.getEmail(),
                "Activá tu acceso al Portal de Inquilinos",
                "Hola " + customer.getFullName() + ",\n\n"
                        + "Activá tu cuenta del Portal de Inquilinos desde el siguiente enlace:\n"
                        + activationUrl + "\n\n"
                        + "El enlace vence el " + expiresAt + ". Si no solicitaste este acceso, podés ignorar este correo."
        );
        auditService.record(account, TenantPortalAuditEventType.INVITATION_SENT,
                delivery.success() ? "Invitación enviada por email" : "Invitación generada; envío de email pendiente o fallido");

        return TenantPortalInvitationResponse.builder()
                .accountId(account.getId())
                .customerId(customerId)
                .email(account.getEmail())
                .expiresAt(expiresAt)
                .emailSent(delivery.success())
                .deliveryMessage(delivery.message())
                .build();
    }

    public TenantPortalAuthResponse activate(TenantPortalActivationRequest request) {
        TenantPortalInvitation invitation = invitationRepository
                .findByTokenHashAndConsumedAtIsNullAndRevokedAtIsNullAndExpiresAtAfter(hash(request.getToken()), LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("La invitación es inválida, fue utilizada o venció"));

        TenantPortalAccount account = invitation.getAccount();
        if (account.getStatus() == TenantPortalAccountStatus.DISABLED) {
            throw new UnauthorizedException("La cuenta del portal está deshabilitada");
        }
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(TenantPortalAccountStatus.ACTIVE);
        account.setActivatedAt(LocalDateTime.now());
        invitation.setConsumedAt(LocalDateTime.now());
        auditService.record(account, TenantPortalAuditEventType.ACCOUNT_ACTIVATED, "Cuenta activada por el inquilino");
        return authResponse(account);
    }

    public TenantPortalAuthResponse login(TenantPortalLoginRequest request) {
        TenantPortalAccount account = accountRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña inválidos"));
        if (account.getStatus() != TenantPortalAccountStatus.ACTIVE
                || account.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new UnauthorizedException("Email o contraseña inválidos");
        }
        account.setLastLoginAt(LocalDateTime.now());
        auditService.record(account, TenantPortalAuditEventType.LOGIN_SUCCEEDED, "Inicio de sesión correcto");
        return authResponse(account);
    }

    public TenantPortalAuthResponse refresh(TenantPortalRefreshRequest request) {
        TenantPortalSessionService.TenantPortalSessionToken session = sessionService.rotate(request.getRefreshToken());
        auditService.record(session.account(), TenantPortalAuditEventType.SESSION_REFRESHED, "Sesión renovada");
        return authResponse(session);
    }

    public void logout(TenantPortalRefreshRequest request) {
        TenantPortalAccount account = sessionService.revoke(request.getRefreshToken());
        if (account != null) auditService.record(account, TenantPortalAuditEventType.LOGOUT, "Sesión cerrada por el inquilino");
    }

    @Transactional(readOnly = true)
    public java.util.Optional<TenantPortalAccountAdminResponse> adminStatus(Long customerId) {
        return accountRepository.findByCustomerId(customerId).map(this::toAdminResponse);
    }

    public TenantPortalAccountAdminResponse updateStatus(Long customerId, TenantPortalAccountStatusUpdateRequest request) {
        TenantPortalAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("El cliente no tiene una cuenta de portal"));
        if (Boolean.TRUE.equals(request.getEnabled())) {
            account.setStatus(account.getPassword() == null
                    ? TenantPortalAccountStatus.PENDING
                    : TenantPortalAccountStatus.ACTIVE);
            auditService.record(account, TenantPortalAuditEventType.ACCOUNT_ENABLED, "Acceso habilitado por administración");
        } else {
            account.setStatus(TenantPortalAccountStatus.DISABLED);
            sessionService.revokeAll(account.getId());
            invitationRepository.findByAccountIdAndConsumedAtIsNullAndRevokedAtIsNull(account.getId())
                    .forEach(invitation -> invitation.setRevokedAt(LocalDateTime.now()));
            auditService.record(account, TenantPortalAuditEventType.ACCOUNT_DISABLED, "Acceso deshabilitado por administración");
        }
        return toAdminResponse(account);
    }

    @Transactional(readOnly = true)
    public TenantPortalProfileResponse profile(Long accountId) {
        return toProfile(findActiveAccount(accountId));
    }

    @Transactional(readOnly = true)
    public TenantPortalAccount findActiveAccount(Long accountId) {
        TenantPortalAccount account = accountRepository.findWithCustomerById(accountId)
                .orElseThrow(() -> new UnauthorizedException("La sesión del portal no es válida"));
        if (account.getStatus() != TenantPortalAccountStatus.ACTIVE) {
            throw new UnauthorizedException("La cuenta del portal no está activa");
        }
        return account;
    }

    private TenantPortalAuthResponse authResponse(TenantPortalAccount account) {
        return authResponse(sessionService.create(account));
    }

    private TenantPortalAuthResponse authResponse(TenantPortalSessionService.TenantPortalSessionToken session) {
        TenantPortalAccount account = session.account();
        return TenantPortalAuthResponse.builder()
                .accessToken(jwtService.generateToken(account))
                .expiresIn(jwtService.getExpiration() / 1000)
                .refreshToken(session.value())
                .refreshExpiresIn(session.expiresIn())
                .profile(toProfile(account))
                .build();
    }

    private TenantPortalProfileResponse toProfile(TenantPortalAccount account) {
        return TenantPortalProfileResponse.builder()
                .accountId(account.getId())
                .customerId(account.getCustomer().getId())
                .fullName(account.getCustomer().getFullName())
                .email(account.getEmail())
                .build();
    }

    private TenantPortalAccountAdminResponse toAdminResponse(TenantPortalAccount account) {
        return TenantPortalAccountAdminResponse.builder()
                .accountId(account.getId())
                .customerId(account.getCustomer().getId())
                .email(account.getEmail())
                .status(account.getStatus())
                .activatedAt(account.getActivatedAt())
                .lastLoginAt(account.getLastLoginAt())
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAuditResponse> audit(Long customerId, int limit) {
        return auditService.findByCustomer(customerId, limit);
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
            throw new java.lang.IllegalStateException("No se pudo preparar el token del portal", ex);
        }
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
