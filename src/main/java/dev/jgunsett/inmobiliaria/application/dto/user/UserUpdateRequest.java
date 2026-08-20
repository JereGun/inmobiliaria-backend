package dev.jgunsett.inmobiliaria.application.dto.user;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserUpdateRequest {

	@Email
	private String email;

	@Size(max = 150)
	private String displayName;

	@Size(max = 50)
	private String whatsapp;

	@Min(0) @Max(100)
	private Integer avatarPositionX;

	@Min(0) @Max(100)
	private Integer avatarPositionY;

	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
	private String password;

	private Role role;

	private Boolean active;

	private Boolean verified;

	private Set<Long> groupIds;
}
