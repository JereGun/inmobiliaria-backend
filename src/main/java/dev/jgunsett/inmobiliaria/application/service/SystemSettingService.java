package dev.jgunsett.inmobiliaria.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingResponse;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingUpdateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.SystemSettingMapper;
import dev.jgunsett.inmobiliaria.domain.entity.SystemSetting;
import dev.jgunsett.inmobiliaria.domain.enums.SettingType;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.exception.ResourceNotFoundException;
import dev.jgunsett.inmobiliaria.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SystemSettingService {

	private final SystemSettingRepository systemSettingRepository;
	private final SystemSettingMapper systemSettingMapper;
	private final ObjectMapper objectMapper;

	public SystemSettingResponse create(SystemSettingCreateRequest request) {
		if (systemSettingRepository.existsByKey(request.getKey())) {
			throw new BusinessException("La configuracion " + request.getKey() + " ya existe");
		}

		validateValue(request.getType(), request.getValue());

		SystemSetting saved = systemSettingRepository.save(systemSettingMapper.toEntity(request));
		return systemSettingMapper.toResponse(saved);
	}

	public SystemSettingResponse update(String key, SystemSettingUpdateRequest request) {
		SystemSetting setting = findEntityByKey(key);

		if (!Boolean.TRUE.equals(setting.getEditable())) {
			throw new BusinessException("La configuracion " + key + " no es editable");
		}

		validateValue(request.getType(), request.getValue());

		systemSettingMapper.updateEntity(setting, request);
		return systemSettingMapper.toResponse(setting);
	}

	@Transactional(readOnly = true)
	public SystemSettingResponse getByKey(String key) {
		return systemSettingMapper.toResponse(findEntityByKey(key));
	}

	@Transactional(readOnly = true)
	public Page<SystemSettingResponse> getAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return systemSettingRepository.findAll(pageable)
				.map(systemSettingMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public SystemSetting findEntityByKey(String key) {
		return systemSettingRepository.findByKey(key)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontro configuracion con clave: " + key));
	}

	private void validateValue(SettingType type, String value) {
		try {
			switch (type) {
				case STRING -> {
				}
				case NUMBER -> new BigDecimal(value);
				case BOOLEAN -> validateBoolean(value);
				case DATE -> LocalDate.parse(value);
				case JSON -> validateJson(value);
			}
		} catch (RuntimeException ex) {
			throw new BusinessException("El valor no coincide con el tipo " + type);
		}
	}

	private void validateBoolean(String value) {
		if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
			throw new BusinessException("El valor no coincide con el tipo BOOLEAN");
		}
	}

	private void validateJson(String value) {
		try {
			objectMapper.readTree(value);
		} catch (JsonProcessingException ex) {
			throw new BusinessException("El valor no coincide con el tipo JSON");
		}
	}
}
