package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.company.CompanyUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.CompanyMapper;
import dev.jgunsett.inmobiliaria.domain.entity.Company;
import dev.jgunsett.inmobiliaria.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

	@Mock
	private CompanyRepository companyRepository;

	private final CompanyMapper companyMapper = new CompanyMapper();

	@Test
	void getReturnsEmptyResponseWhenCompanyDoesNotExist() {
		when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

		CompanyService service = new CompanyService(companyRepository, companyMapper);

		var response = service.get();

		assertThat(response.getId()).isNull();
		assertThat(response.getName()).isNull();
	}

	@Test
	void updateCreatesCompanyWhenItDoesNotExist() {
		CompanyUpdateRequest request = validRequest();
		when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
		when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
			Company company = invocation.getArgument(0);
			company.setId(1L);
			return company;
		});

		CompanyService service = new CompanyService(companyRepository, companyMapper);

		var response = service.update(request);

		ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
		verify(companyRepository).save(captor.capture());

		assertThat(captor.getValue().getName()).isEqualTo("Inmobiliaria Demo");
		assertThat(captor.getValue().getTaxId()).isEqualTo("30-12345678-9");
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getEmail()).isEqualTo("info@demo.com");
	}

	@Test
	void updateReusesExistingCompany() {
		Company existing = Company.builder()
				.id(1L)
				.name("Nombre anterior")
				.email("anterior@demo.com")
				.build();
		CompanyUpdateRequest request = validRequest();

		when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existing));
		when(companyRepository.save(existing)).thenReturn(existing);

		CompanyService service = new CompanyService(companyRepository, companyMapper);

		var response = service.update(request);

		assertThat(existing.getName()).isEqualTo("Inmobiliaria Demo");
		assertThat(existing.getEmail()).isEqualTo("info@demo.com");
		assertThat(response.getName()).isEqualTo("Inmobiliaria Demo");
	}

	private CompanyUpdateRequest validRequest() {
		CompanyUpdateRequest request = new CompanyUpdateRequest();
		request.setName("Inmobiliaria Demo");
		request.setTaxId("30-12345678-9");
		request.setFiscalCondition("Responsable Inscripto");
		request.setStreet("San Martin");
		request.setNumber("123");
		request.setCity("Cordoba");
		request.setProvince("Cordoba");
		request.setCountry("Argentina");
		request.setEmail("info@demo.com");
		request.setPhone("+54 351 555-0101");
		request.setWebsite("https://demo.com");
		request.setLogoUrl("https://demo.com/logo.png");
		return request;
	}
}
