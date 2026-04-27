package dev.jgunsett.inmobiliaria.application.dto.user;

import java.time.LocalDateTime;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
	
	private Long id;
	private String email;
	private Role role;
	private Boolean active;
	private Boolean verified;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime lastLogin;
}
