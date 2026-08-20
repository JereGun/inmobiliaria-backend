package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.WhatsAppMessageStatus;
import dev.jgunsett.inmobiliaria.domain.enums.WhatsAppMessageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "whatsapp_message_log", indexes = {
        @Index(name = "idx_whatsapp_log_customer", columnList = "customer_id"),
        @Index(name = "idx_whatsapp_log_invoice", columnList = "invoice_id"),
        @Index(name = "idx_whatsapp_log_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppMessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 40)
    private WhatsAppMessageType type;

    @Column(name = "deduplication_key", nullable = false, unique = true, length = 160)
    private String deduplicationKey;

    @Column(name = "recipient_phone", nullable = false, length = 30)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WhatsAppMessageStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "provider_message_id", length = 160)
    private String providerMessageId;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    private LocalDateTime sentAt;
    private LocalDateTime lastAttemptAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    private LocalDateTime modificationDate;

    @PrePersist
    public void onCreate() {
        creationDate = LocalDateTime.now();
        modificationDate = LocalDateTime.now();
        if (status == null) status = WhatsAppMessageStatus.PENDING;
        if (attempts == null) attempts = 0;
    }

    @PreUpdate
    public void onUpdate() {
        modificationDate = LocalDateTime.now();
    }
}
