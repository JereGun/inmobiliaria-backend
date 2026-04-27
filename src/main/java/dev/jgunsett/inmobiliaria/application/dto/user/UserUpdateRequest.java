package dev.jgunsett.inmobiliaria.application.dto.user;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

	@Email
	private String email;

	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	private String password;

	private Role role;

	private Boolean active;

	private Boolean verified;
}
