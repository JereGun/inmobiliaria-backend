package dev.jgunsett.inmobiliaria.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Snapshot historico de la liquidacion economica de un contrato para un
 * propietario en un periodo determinado.
 */
@Entity
@Data
@Table(
        name = "settlement",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_settlement_contract_period",
                        columnNames = {"contract_id", "period"}
                )
        },
        indexes = {
                @Index(name = "idx_settlement_owner", columnList = "owner_id"),
                @Index(name = "idx_settlement_contract", columnList = "contract_id"),
                @Index(name = "idx_settlement_period", columnList = "period")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @NotNull
    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    /** Periodo liquidado con formato YYYY-MM. */
    @NotBlank
    @Column(nullable = false, length = 7)
    private String period;

    /** Total cobrado al inquilino en el periodo. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCharged;

    /** Comision retenida por la inmobiliaria. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal commission;

    /** Impuestos aplicados sobre la liquidacion. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tax;

    /** Monto final a pagar al propietario. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netPay;

    private LocalDateTime creationDate;

    private LocalDateTime modificationDate;

    @PrePersist
    public void onCreate() {
        this.creationDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.modificationDate = LocalDateTime.now();
    }
}
