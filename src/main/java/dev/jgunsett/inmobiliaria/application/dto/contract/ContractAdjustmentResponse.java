package dev.jgunsett.inmobiliaria.application.dto.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.AdjustmentType;
import lombok.*;

@Getter
@Builder
public class ContractAdjustmentResponse {

    private Long id;
    private Long contractId;
    private LocalDate effectiveDate;
    private AdjustmentType adjustmentType;
    private BigDecimal value;
    private Boolean active;
    private LocalDateTime creationDate;
}