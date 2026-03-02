package dev.jgunsett.inmobiliaria.application.dto.contract;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractUpdateRequest {
	
	private LocalDate endDate;
	
	private BigDecimal baseRentalAmount;
	
	private BigDecimal lateFeePercentage;

}
