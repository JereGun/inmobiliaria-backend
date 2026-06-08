package dev.jgunsett.inmobiliaria.domain.entity;

import dev.jgunsett.inmobiliaria.domain.enums.ContractEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractEventType eventType;

    @Column(length = 500)
    private String details;

    @Column(length = 255)
    private String performedBy;

    private LocalDateTime occurredAt;

    @PrePersist
    public void onCreate() {
        this.occurredAt = LocalDateTime.now();
    }
}
