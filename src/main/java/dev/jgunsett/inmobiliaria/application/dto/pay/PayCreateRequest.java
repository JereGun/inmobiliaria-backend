package dev.jgunsett.inmobiliaria.application.dto.pay;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.jgunsett.inmobiliaria.domain.enums.PayMedium;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayCreateRequest {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate date;

    @NotNull
    private PayMedium medium;

    @NotNull
    private Long invoiceId;
}