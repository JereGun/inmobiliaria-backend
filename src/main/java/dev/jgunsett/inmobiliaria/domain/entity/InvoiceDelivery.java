package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.InvoiceDeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "invoice_delivery",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_delivery_invoice_channel",
                columnNames = {"invoice_id", "channel"}
        ),
        indexes = {
                @Index(name = "idx_invoice_delivery_status", columnList = "status"),
                @Index(name = "idx_invoice_delivery_invoice", columnList = "invoice_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String channel = "EMAIL";

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceDeliveryStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    private LocalDateTime sentAt;
    private LocalDateTime lastAttemptAt;

    @Column(length = 1000)
    private String lastError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    private LocalDateTime modificationDate;

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        if (this.status == null) this.status = InvoiceDeliveryStatus.PENDING;
        if (this.attempts == null) this.attempts = 0;
        if (this.channel == null || this.channel.isBlank()) this.channel = "EMAIL";
    }

    @PreUpdate
    public void onUpdate() {
        this.modificationDate = LocalDateTime.now();
    }
}
