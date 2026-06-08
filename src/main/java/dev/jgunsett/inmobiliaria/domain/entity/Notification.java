package dev.jgunsett.inmobiliaria.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "notification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_user_contract_type_due_date",
                        columnNames = {"user_id", "contract_id", "type", "due_date"}
                )
        },
        indexes = {
                @Index(name = "idx_notification_user_read", columnList = "user_id, read_status"),
                @Index(name = "idx_notification_due_date", columnList = "due_date")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private NotificationType type;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String message;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Builder.Default
    @Column(name = "read_status", nullable = false)
    private Boolean read = false;

    private LocalDateTime readAt;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
        if (this.read == null) {
            this.read = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.modificationDate = LocalDateTime.now();
    }
}
