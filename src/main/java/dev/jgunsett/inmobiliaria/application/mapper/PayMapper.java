package dev.jgunsett.inmobiliaria.application.mapper;

import dev.jgunsett.inmobiliaria.application.dto.pay.PayResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Pay;

public class PayMapper {

    public static PayResponse toResponse(Pay pay) {
        return PayResponse.builder()
                .id(pay.getId())
                .amount(pay.getAmount())
                .date(pay.getDate())
                .medium(pay.getMedium())
                .invoiceId(pay.getInvoice().getId())
                .build();
    }
}