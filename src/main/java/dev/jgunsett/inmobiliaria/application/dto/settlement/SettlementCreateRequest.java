package dev.jgunsett.inmobiliaria.application.dto.settlement;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettlementCreateRequest {

    @NotNull
    private Long contractId;

    @NotBlank
    private String period;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal commissionPercentage;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal taxPercentage;
}
