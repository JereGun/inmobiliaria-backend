package dev.jgunsett.inmobiliaria.application.dto.contract;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractUpdateRequest {
	
	private LocalDate endDate;
	
	private BigDecimal baseRentalAmount;
	

	@DecimalMin(value = "0.0", inclusive = true)
	private BigDecimal lateFeePercentage;

	@Min(1)
	@Max(31)
	private Integer paymentDueDay;

}
