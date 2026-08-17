package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalSession;

public interface TenantPortalSessionRepository extends JpaRepository<TenantPortalSession, Long> {
    Optional<TenantPortalSession> findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(String tokenHash, LocalDateTime now);
    List<TenantPortalSession> findByAccountIdAndRevokedAtIsNull(Long accountId);
}
