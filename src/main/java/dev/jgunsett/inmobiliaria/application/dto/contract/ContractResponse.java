package dev.jgunsett.inmobiliaria.application.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.BillingFrequency;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import dev.jgunsett.inmobiliaria.domain.enums.ContractType;
import dev.jgunsett.inmobiliaria.domain.enums.Currency;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {

	private Long id;
	
	private Long propertyId;
	private String propertyName;
	
	private Long ownerId;
	private String ownerFullName;
	
	private Long tenantId;
	private String tenantFullName;
	
	private LocalDate startDate;
	private LocalDate endDatel;
	
	private BigDecimal baseRentalAmount;
	private LocalDate firstAdjustmentDate;
	private Currency currency;
	private BillingFrequency billingFrequency;
	
	private ContractStatus status;
	private ContractType contractType;
	
	private BigDecimal lateFeePercentage;
	
	private LocalDateTime creationDate;
	private LocalDateTime modificationDate;
}
