package dev.jgunsett.inmobiliaria.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.BillingFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.ContractType;
import dev.jgunsett.inmobiliaria.domain.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@Table(name = "contract")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contract {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "property_id", nullable = false)
	private Property property;
	
	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Customer owner;
	
	@ManyToOne
	@JoinColumn(name = "tenant_id", nullable = false)
	private Customer tenant;
	
	@NotNull
	private LocalDate startDate;

	@NotNull
	private LocalDate endDate;
	
	private BigDecimal baseRentalAmount;
	
    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_frequency", nullable = true)
    private AdjustmentFrequency adjustmentFrequency;

    @Column(name = "first_adjustment_date", nullable = true)
    private LocalDate firstAdjustmentDate;
	
	@Enumerated(EnumType.STRING)
	private Currency currency;
	
	@Enumerated(EnumType.STRING)
	private BillingFrequency billingFrequency;
	
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private ContractStatus status = ContractStatus.DRAFT;
	
	@Enumerated(EnumType.STRING)
	private ContractType contractType;
	
	// Interes o recargo por mora
	private BigDecimal lateFeePercentage;
	
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
