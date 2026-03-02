package dev.jgunsett.inmobiliaria.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "contract_adjustment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @NotNull
    private LocalDate effectiveDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AdjustmentType adjustmentType;

    @NotNull
    @Column(precision = 15, scale = 2)
    private BigDecimal value;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime creationDate;

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
}
