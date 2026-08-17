package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalSession;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
import dev.jgunsett.inmobiliaria.repository.TenantPortalSessionRepository;

@ExtendWith(MockitoExtension.class)
class TenantPortalSessionServiceTest {

    @Mock private TenantPortalSessionRepository sessionRepository;

    @Test
    void rotateRevokesPreviousRefreshTokenAndCreatesAnother() {
        TenantPortalAccount account = TenantPortalAccount.builder()
                .id(9L)
                .status(TenantPortalAccountStatus.ACTIVE)
                .build();
        TenantPortalSession current = TenantPortalSession.builder()
                .id(4L)
                .account(account)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        when(sessionRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(anyString(), any()))
                .thenReturn(Optional.of(current));

        TenantPortalSessionService.TenantPortalSessionToken refreshed = service().rotate("old-refresh-token");

        assertThat(current.getRevokedAt()).isNotNull();
        assertThat(refreshed.value()).isNotBlank().isNotEqualTo("old-refresh-token");
        verify(sessionRepository).save(any(TenantPortalSession.class));
    }

    private TenantPortalSessionService service() {
        TenantPortalSessionService service = new TenantPortalSessionService(sessionRepository);
        ReflectionTestUtils.setField(service, "expirationMillis", 2_592_000_000L);
        return service;
    }
}
