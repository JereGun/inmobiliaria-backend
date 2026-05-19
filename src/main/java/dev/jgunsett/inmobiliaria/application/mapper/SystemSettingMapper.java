package dev.jgunsett.inmobiliaria.application.mapper;

import org.springframework.stereotype.Component;

import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingResponse;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingUpdateRequest;
import dev.jgunsett.inmobiliaria.domain.entity.SystemSetting;

@Component
public class SystemSettingMapper {

	public SystemSetting toEntity(SystemSettingCreateRequest request) {
		if (request == null) {
			return null;
		}

		return SystemSetting.builder()
				.key(request.getKey())
				.value(request.getValue())
				.type(request.getType())
				.description(request.getDescription())
				.editable(request.getEditable() == null || request.getEditable())
				.build();
	}

	public void updateEntity(SystemSetting setting, SystemSettingUpdateRequest request) {
		if (setting == null || request == null) {
			return;
		}

		setting.setValue(request.getValue());
		setting.setType(request.getType());
		setting.setDescription(request.getDescription());
		if (request.getEditable() != null) {
			setting.setEditable(request.getEditable());
		}
	}

	public SystemSettingResponse toResponse(SystemSetting setting) {
		if (setting == null) {
			return null;
		}

		return SystemSettingResponse.builder()
				.id(setting.getId())
				.key(setting.getKey())
				.value(setting.getValue())
				.type(setting.getType())
				.description(setting.getDescription())
				.editable(setting.getEditable())
				.creationDate(setting.getCreationDate())
				.modificationDate(setting.getModificationDate())
				.build();
	}
}
