package dev.jgunsett.inmobiliaria.application.dto.auth;

import dev.jgunsett.inmobiliaria.application.dto.user.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

	private String token;
	private String tokenType;
	private Long expiresIn;
	private UserResponse user;
}
