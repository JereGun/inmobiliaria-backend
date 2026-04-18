package dev.jgunsett.inmobiliaria.application.mapper;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;

public class ContractMapper {
	
	// TODO: Chekear la factibilidad de agregar Mapper "toEntity", "updateEntity".
	
	public static ContractResponse toResponse(Contract c) {
		
		return ContractResponse.builder()
				.id(c.getId())
				.propertyId(c.getProperty().getId())
				.propertyName(c.getProperty().getName())
				.ownerId(c.getOwner().getId())
				.ownerFullName(c.getOwner().getFullName())
				.tenantId(c.getTenant().getId())
				.tenantFullName(c.getTenant().getFullName())
				.startDate(c.getStartDate())
				.endDate(c.getEndDate())
				.baseRentalAmount(c.getBaseRentalAmount())
				.firstAdjustmentDate(c.getFirstAdjustmentDate())
				.currency(c.getCurrency())
				.billingFrequency(c.getBillingFrequency())
				.status(c.getStatus())
				.contractType(c.getContractType())
				.lateFeePercentage(c.getLateFeePercentage())
				.creationDate(c.getCreationDate())
				.modificationDate(c.getModificationDate())
				.build();
	}

}
