package dev.jgunsett.inmobiliaria.application.dto.invoice;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvoiceLateFeeRequest {

    @NotNull
    @DecimalMin(value = "0.0001")
    @Digits(integer = 4, fraction = 4)
    private BigDecimal dailyPercentage;
}
