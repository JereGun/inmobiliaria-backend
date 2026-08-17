package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalInvitation;

public interface TenantPortalInvitationRepository extends JpaRepository<TenantPortalInvitation, Long> {
    List<TenantPortalInvitation> findByAccountIdAndConsumedAtIsNullAndRevokedAtIsNull(Long accountId);

    Optional<TenantPortalInvitation> findByTokenHashAndConsumedAtIsNullAndRevokedAtIsNullAndExpiresAtAfter(
            String tokenHash,
            LocalDateTime now
    );
}
