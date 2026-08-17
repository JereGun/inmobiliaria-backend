package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.TenantPortalAccountStatus;
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
@Table(name = "tenant_portal_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPortalAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantPortalAccountStatus status = TenantPortalAccountStatus.PENDING;

    private LocalDateTime activatedAt;
    private LocalDateTime lastLoginAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;

    @jakarta.persistence.PrePersist
    public void onCreate() {
        creationDate = LocalDateTime.now();
        modificationDate = LocalDateTime.now();
        if (status == null) status = TenantPortalAccountStatus.PENDING;
    }

    @jakarta.persistence.PreUpdate
    public void onUpdate() {
        modificationDate = LocalDateTime.now();
    }
}
