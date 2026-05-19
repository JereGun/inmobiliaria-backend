package dev.jgunsett.inmobiliaria.application.dto.setting;

import dev.jgunsett.inmobiliaria.domain.enums.SettingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemSettingUpdateRequest {

	@NotBlank
	private String value;

	@NotNull
	private SettingType type;

	private String description;

	private Boolean editable;
}
