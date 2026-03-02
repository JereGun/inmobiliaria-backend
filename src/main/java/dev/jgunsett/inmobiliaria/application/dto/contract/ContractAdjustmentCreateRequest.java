package dev.jgunsett.inmobiliaria.application.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ContractAdjustmentCreateRequest {

    @NotNull
    private LocalDate effectiveDate;

    @NotNull
    private AdjustmentType adjustmentType;

    @NotNull
    @Positive
    private BigDecimal value;
}