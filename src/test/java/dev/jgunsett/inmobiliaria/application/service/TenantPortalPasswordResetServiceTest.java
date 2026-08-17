package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalForgotPasswordRequest;
import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalResetPasswordRequest;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalPasswordResetToken;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
import dev.jgunsett.inmobiliaria.repository.TenantPortalAccountRepository;
import dev.jgunsett.inmobiliaria.repository.TenantPortalPasswordResetTokenRepository;

@ExtendWith(MockitoExtension.class)
class TenantPortalPasswordResetServiceTest {

    @Mock private TenantPortalAccountRepository accountRepository;
    @Mock private TenantPortalPasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailSenderService emailSenderService;
    @Mock private TenantPortalAuditService auditService;

    @Test
    void requestResetDoesNotRevealUnknownEmail() {
        TenantPortalForgotPasswordRequest request = new TenantPortalForgotPasswordRequest();
        request.setEmail("unknown@example.com");
        when(accountRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        service().requestReset(request);

        verify(emailSenderService, never()).sendNotificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resetChangesPasswordAndConsumesToken() {
        TenantPortalAccount account = TenantPortalAccount.builder()
                .id(3L)
                .status(TenantPortalAccountStatus.ACTIVE)
                .build();
        TenantPortalPasswordResetToken token = TenantPortalPasswordResetToken.builder()
                .account(account)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        TenantPortalResetPasswordRequest request = new TenantPortalResetPasswordRequest();
        request.setToken("raw-token");
        request.setNewPassword("new-secret");
        when(tokenRepository.findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-password");

        service().reset(request);

        assertThat(account.getPassword()).isEqualTo("encoded-password");
        assertThat(token.getUsedAt()).isNotNull();
        verify(passwordEncoder).encode("new-secret");
    }

    private TenantPortalPasswordResetService service() {
        return new TenantPortalPasswordResetService(accountRepository, tokenRepository, passwordEncoder, emailSenderService, auditService);
    }
}
