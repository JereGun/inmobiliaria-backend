package dev.jgunsett.inmobiliaria.application.mapper;

import org.springframework.stereotype.Component;

import dev.jgunsett.inmobiliaria.application.dto.settlement.SettlementResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.entity.Settlement;

@Component
public class SettlementMapper {

    public SettlementResponse toResponse(Settlement settlement, Contract contract) {
        Customer owner = contract.getOwner();
        Property property = contract.getProperty();

        return SettlementResponse.builder()
                .id(settlement.getId())
                .ownerId(settlement.getOwnerId())
                .ownerName(owner.getFullName())
                .contractId(settlement.getContractId())
                .propertyName(property.getName())
                .period(settlement.getPeriod())
                .totalCharged(settlement.getTotalCharged())
                .commission(settlement.getCommission())
                .tax(settlement.getTax())
                .netPay(settlement.getNetPay())
                .creationDate(settlement.getCreationDate())
                .modificationDate(settlement.getModificationDate())
                .build();
    }
}
