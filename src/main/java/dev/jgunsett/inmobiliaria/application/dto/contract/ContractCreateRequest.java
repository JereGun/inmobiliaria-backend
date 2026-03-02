package dev.jgunsett.inmobiliaria.application.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.jgunsett.inmobiliaria.domain.enums.BillingFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.ContractType;
import dev.jgunsett.inmobiliaria.domain.enums.Currency;
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
	private Currency currency;
	
	@NotNull
	private BillingFrequency billingFrequency;
	
	@NotNull
	private ContractType contractType;
	
	private BigDecimal lateFeePercentage;
}
