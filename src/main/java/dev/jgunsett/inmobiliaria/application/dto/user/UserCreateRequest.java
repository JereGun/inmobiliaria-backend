package dev.jgunsett.inmobiliaria.application.dto.user;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
	
	@Email
	@NotBlank
	private String email;
	
	@NotBlank
	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	private String password;

	@NotNull
	private Role role;
	
}
