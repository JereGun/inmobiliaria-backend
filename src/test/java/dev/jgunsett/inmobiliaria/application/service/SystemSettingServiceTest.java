package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.SystemSettingMapper;
import dev.jgunsett.inmobiliaria.domain.entity.SystemSetting;
import dev.jgunsett.inmobiliaria.domain.enums.SettingType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.repository.SystemSettingRepository;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceTest {

	@Mock
	private SystemSettingRepository systemSettingRepository;

	private final SystemSettingMapper systemSettingMapper = new SystemSettingMapper();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void createPersistsValidSetting() {
		SystemSettingCreateRequest request = createRequest("invoice.default-currency", "ARS", SettingType.STRING);

		when(systemSettingRepository.existsByKey("invoice.default-currency")).thenReturn(false);
		when(systemSettingRepository.save(any(SystemSetting.class))).thenAnswer(invocation -> {
			SystemSetting setting = invocation.getArgument(0);
			setting.setId(1L);
			return setting;
		});

		SystemSettingService service = new SystemSettingService(systemSettingRepository, systemSettingMapper, objectMapper);

		var response = service.create(request);

		ArgumentCaptor<SystemSetting> captor = ArgumentCaptor.forClass(SystemSetting.class);
		verify(systemSettingRepository).save(captor.capture());

		assertThat(captor.getValue().getKey()).isEqualTo("invoice.default-currency");
		assertThat(captor.getValue().getEditable()).isTrue();
		assertThat(response.getId()).isEqualTo(1L);
	}

	@Test
	void createRejectsDuplicatedKey() {
		SystemSettingCreateRequest request = createRequest("invoice.default-currency", "ARS", SettingType.STRING);

		when(systemSettingRepository.existsByKey("invoice.default-currency")).thenReturn(true);

		SystemSettingService service = new SystemSettingService(systemSettingRepository, systemSettingMapper, objectMapper);

		assertThatThrownBy(() -> service.create(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("ya existe");

		verify(systemSettingRepository, never()).save(any(SystemSetting.class));
	}

	@Test
	void createRejectsInvalidNumberValue() {
		SystemSettingCreateRequest request = createRequest("invoice.default-tax-rate", "abc", SettingType.NUMBER);

		when(systemSettingRepository.existsByKey("invoice.default-tax-rate")).thenReturn(false);

		SystemSettingService service = new SystemSettingService(systemSettingRepository, systemSettingMapper, objectMapper);

		assertThatThrownBy(() -> service.create(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("NUMBER");

		verify(systemSettingRepository, never()).save(any(SystemSetting.class));
	}

	@Test
	void updateRejectsNonEditableSetting() {
		SystemSetting existing = SystemSetting.builder()
				.id(1L)
				.key("security.jwt-secret")
				.value("masked")
				.type(SettingType.STRING)
				.editable(false)
				.build();
		SystemSettingUpdateRequest request = updateRequest("new-value", SettingType.STRING);

		when(systemSettingRepository.findByKey("security.jwt-secret")).thenReturn(Optional.of(existing));

		SystemSettingService service = new SystemSettingService(systemSettingRepository, systemSettingMapper, objectMapper);

		assertThatThrownBy(() -> service.update("security.jwt-secret", request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("no es editable");
	}

	@Test
	void updateAcceptsValidJson() {
		SystemSetting existing = SystemSetting.builder()
				.id(1L)
				.key("mail.templates")
				.value("{}")
				.type(SettingType.JSON)
				.editable(true)
				.build();
		SystemSettingUpdateRequest request = updateRequest("{\"welcome\":\"Hola\"}", SettingType.JSON);

		when(systemSettingRepository.findByKey("mail.templates")).thenReturn(Optional.of(existing));

		SystemSettingService service = new SystemSettingService(systemSettingRepository, systemSettingMapper, objectMapper);

		var response = service.update("mail.templates", request);

		assertThat(existing.getValue()).isEqualTo("{\"welcome\":\"Hola\"}");
		assertThat(response.getType()).isEqualTo(SettingType.JSON);
	}

	private SystemSettingCreateRequest createRequest(String key, String value, SettingType type) {
		SystemSettingCreateRequest request = new SystemSettingCreateRequest();
		request.setKey(key);
		request.setValue(value);
		request.setType(type);
		request.setDescription("Descripcion");
		return request;
	}

	private SystemSettingUpdateRequest updateRequest(String value, SettingType type) {
		SystemSettingUpdateRequest request = new SystemSettingUpdateRequest();
		request.setValue(value);
		request.setType(type);
		request.setDescription("Descripcion actualizada");
		return request;
	}
}
