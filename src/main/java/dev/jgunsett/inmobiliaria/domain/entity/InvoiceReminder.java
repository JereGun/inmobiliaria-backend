package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.InvoiceReminderStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceReminderType;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "invoice_reminder",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_reminder_invoice_type_date",
                columnNames = {"invoice_id", "reminder_type", "scheduled_for"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 30)
    private InvoiceReminderType type;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDate scheduledFor;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceReminderStatus status;

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
        creationDate = LocalDateTime.now();
        modificationDate = LocalDateTime.now();
        if (status == null) status = InvoiceReminderStatus.PENDING;
        if (attempts == null) attempts = 0;
    }

    @PreUpdate
    public void onUpdate() {
        modificationDate = LocalDateTime.now();
    }
}
