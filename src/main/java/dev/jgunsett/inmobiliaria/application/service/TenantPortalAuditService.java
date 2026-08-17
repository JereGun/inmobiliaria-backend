package dev.jgunsett.inmobiliaria.application.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.tenantportal.TenantPortalAuditResponse;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAccount;
import dev.jgunsett.inmobiliaria.domain.entity.TenantPortalAuditLog;
import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAuditEventType;
import dev.jgunsett.inmobiliaria.repository.TenantPortalAuditLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantPortalAuditService {

    private final TenantPortalAuditLogRepository auditLogRepository;

    public void record(TenantPortalAccount account, TenantPortalAuditEventType eventType, String detail) {
        auditLogRepository.save(TenantPortalAuditLog.builder()
                .account(account)
                .eventType(eventType)
                .detail(detail)
                .build());
    }

    @Transactional(readOnly = true)
    public List<TenantPortalAuditResponse> findByCustomer(Long customerId, int limit) {
        return auditLogRepository.findByAccountCustomerIdOrderByOccurredAtDesc(
                        customerId,
                        PageRequest.of(0, Math.min(Math.max(limit, 1), 50))
                ).stream()
                .map(event -> TenantPortalAuditResponse.builder()
                        .id(event.getId())
                        .eventType(event.getEventType())
                        .detail(event.getDetail())
                        .occurredAt(event.getOccurredAt())
                        .build())
                .toList();
    }
}
