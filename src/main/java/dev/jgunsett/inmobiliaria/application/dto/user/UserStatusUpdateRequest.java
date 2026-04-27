package dev.jgunsett.inmobiliaria.application.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {

	@NotNull
	private Boolean active;
}
