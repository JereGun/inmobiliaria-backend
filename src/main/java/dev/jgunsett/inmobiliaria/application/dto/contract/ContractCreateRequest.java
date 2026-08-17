package dev.jgunsett.inmobiliaria.application.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.BillingFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.ContractType;
import dev.jgunsett.inmobiliaria.domain.enums.Currency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractCreateRequest {

	@NotNull
	private Long propertyId;
	
	@NotNull
	private Long tenantId;
	
	@NotNull
	private LocalDate startDate;
	
	@NotNull
	private LocalDate endDate;
	
	@NotNull
	private BigDecimal baseRentalAmount;
	
	@NotNull
	private LocalDate firstAdjustmentDate;

	@NotNull
	private AdjustmentFrequency adjustmentFrequency;
	
	@NotNull
	private Currency currency;
	
	@NotNull
	private BillingFrequency billingFrequency;

	@Min(1)
	@Max(31)
	private Integer paymentDueDay = 1;
	
	@NotNull
	private ContractType contractType;
	

	@DecimalMin(value = "0.0", inclusive = true)
	private BigDecimal lateFeePercentage;
}
