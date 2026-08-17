package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalPasswordResetToken;

public interface TenantPortalPasswordResetTokenRepository extends JpaRepository<TenantPortalPasswordResetToken, Long> {
    List<TenantPortalPasswordResetToken> findByAccountIdAndUsedAtIsNull(Long accountId);

    Optional<TenantPortalPasswordResetToken> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
            String tokenHash,
            LocalDateTime now
    );
}
