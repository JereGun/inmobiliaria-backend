package dev.jgunsett.inmobiliaria.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAuditLog;

public interface TenantPortalAuditLogRepository extends JpaRepository<TenantPortalAuditLog, Long> {
    List<TenantPortalAuditLog> findByAccountCustomerIdOrderByOccurredAtDesc(Long customerId, Pageable pageable);
}
