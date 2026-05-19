package dev.jgunsett.inmobiliaria.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import dev.jgunsett.inmobiliaria.application.dto.company.CompanyUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.CompanyMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Company;
import dev.jgunsett.inmobiliaria.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

	private final CompanyRepository companyRepository;
	private final CompanyMapper companyMapper;

	@Transactional(readOnly = true)
	public CompanyResponse get() {
		return companyMapper.toResponse(findCurrentOrNull());
	}

	public CompanyResponse update(CompanyUpdateRequest request) {
		Company company = findCurrentOrNull();

		if (company == null) {
			company = companyMapper.toEntity(request);
		} else {
			companyMapper.updateEntity(company, request);
		}

		return companyMapper.toResponse(companyRepository.save(company));
	}

	private Company findCurrentOrNull() {
		return companyRepository.findFirstByOrderByIdAsc()
				.orElse(null);
	}
}
