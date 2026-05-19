package dev.jgunsett.inmobiliaria.application.mapper;

import org.springframework.stereotype.Component;

import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import dev.jgunsett.inmobiliaria.application.dto.company.CompanyUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.Company;

@Component
public class CompanyMapper {

	public Company toEntity(CompanyUpdateRequest request) {
		if (request == null) {
			return null;
		}

		Company company = new Company();
		updateEntity(company, request);
		return company;
	}

	public void updateEntity(Company company, CompanyUpdateRequest request) {
		if (company == null || request == null) {
			return;
		}

		company.setName(request.getName());
		company.setTaxId(request.getTaxId());
		company.setFiscalCondition(request.getFiscalCondition());
		company.setStreet(request.getStreet());
		company.setNumber(request.getNumber());
		company.setCity(request.getCity());
		company.setProvince(request.getProvince());
		company.setCountry(request.getCountry());
		company.setEmail(request.getEmail());
		company.setPhone(request.getPhone());
		company.setWebsite(request.getWebsite());
		company.setLogoUrl(request.getLogoUrl());
	}

	public CompanyResponse toResponse(Company company) {
		if (company == null) {
			return CompanyResponse.builder().build();
		}

		return CompanyResponse.builder()
				.id(company.getId())
				.name(company.getName())
				.taxId(company.getTaxId())
				.fiscalCondition(company.getFiscalCondition())
				.street(company.getStreet())
				.number(company.getNumber())
				.city(company.getCity())
				.province(company.getProvince())
				.country(company.getCountry())
				.email(company.getEmail())
				.phone(company.getPhone())
				.website(company.getWebsite())
				.logoUrl(company.getLogoUrl())
				.creationDate(company.getCreationDate())
				.modificationDate(company.getModificationDate())
				.build();
	}
}
