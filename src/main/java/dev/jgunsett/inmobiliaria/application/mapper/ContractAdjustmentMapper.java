package dev.jgunsett.inmobiliaria.application.mapper;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractAdjustmentResponse;
import dev.jgunsett.inmobiliaria.domain.entity.ContractAdjustment;

public class ContractAdjustmentMapper {

    public static ContractAdjustmentResponse toResponse(ContractAdjustment entity) {
        return ContractAdjustmentResponse.builder()
                .id(entity.getId())
                .contractId(entity.getContract().getId())
                .effectiveDate(entity.getEffectiveDate())
                .adjustmentType(entity.getAdjustmentType())
                .value(entity.getValue())
                .active(entity.getActive())
                .creationDate(entity.getCreationDate())
                .build();
    }
}
