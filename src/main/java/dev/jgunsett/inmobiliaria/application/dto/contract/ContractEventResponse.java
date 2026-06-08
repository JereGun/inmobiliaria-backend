package dev.jgunsett.inmobiliaria.application.dto.contract;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContractEventResponse {

    private Long id;
    private String eventType;
    private String details;
    private String performedBy;
    private LocalDateTime occurredAt;
}
