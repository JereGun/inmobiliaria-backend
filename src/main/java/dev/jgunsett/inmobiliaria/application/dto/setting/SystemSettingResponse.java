package dev.jgunsett.inmobiliaria.application.dto.setting;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.SettingType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettingResponse {

	private Long id;
	private String key;
	private String value;
	private SettingType type;
	private String description;
	private Boolean editable;
	private LocalDateTime creationDate;
	private LocalDateTime modificationDate;
}
