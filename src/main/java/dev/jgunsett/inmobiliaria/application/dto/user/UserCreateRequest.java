package dev.jgunsett.inmobiliaria.application.dto.user;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserCreateRequest {
	
	@Email
	@NotBlank
	private String email;

	@Size(max = 150)
	private String displayName;

	@Size(max = 50)
	private String whatsapp;

	@Min(0) @Max(100)
	private Integer avatarPositionX = 50;

	@Min(0) @Max(100)
	private Integer avatarPositionY = 50;
	
	@NotBlank
	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	private String password;

	@NotNull
	private Role role;

	private Set<Long> groupIds = new HashSet<>();
	
}
