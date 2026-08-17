package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAuditEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_portal_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPortalAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private TenantPortalAccount account;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private TenantPortalAuditEventType eventType;

    @Column(length = 500)
    private String detail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @jakarta.persistence.PrePersist
    public void onCreate() {
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }
}
