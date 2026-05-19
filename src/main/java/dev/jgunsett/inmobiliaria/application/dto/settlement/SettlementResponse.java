package dev.jgunsett.inmobiliaria.application.dto.settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettlementResponse {

    private Long id;
    private Long ownerId;
    private String ownerName;
    private Long contractId;
    private String propertyName;
    private String period;
    private BigDecimal totalCharged;
    private BigDecimal commission;
    private BigDecimal tax;
    private BigDecimal netPay;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
}
