package dev.jgunsett.inmobiliaria.application.dto.pay;

import java.math.BigDecimal;
import java.time.LocalDate;

import dev.jgunsett.inmobiliaria.domain.enums.PayMedium;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private PayMedium medium;
    private Long invoiceId;
}