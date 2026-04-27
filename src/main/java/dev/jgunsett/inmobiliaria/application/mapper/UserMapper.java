package dev.jgunsett.inmobiliaria.application.mapper;

import org.springframework.stereotype.Component;

import dev.jgunsett.inmobiliaria.application.dto.user.UserCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserResponse;
import dev.jgunsett.inmobiliaria.domain.entity.User;

@Component
public class UserMapper {
	
	public User toEntity(UserCreateRequest request, String encodedPassword) {
		if (request == null) {
			return null;
		}

		return User.builder()
				.email(request.getEmail())
				.password(encodedPassword)
				.role(request.getRole())
				.active(true)
				.verified(false)
				.build();
	}

	public UserResponse toResponse(User u) {
		if (u == null) {
			return null;
		}
		
		return UserResponse.builder()
				.id(u.getId())
				.email(u.getEmail())
				.role(u.getRole())
				.active(u.getActive())
				.verified(u.getVerified())
				.createdAt(u.getCreatedAt())
				.updatedAt(u.getUpdatedAt())
				.lastLogin(u.getLastLogin())
				.build();
	}
}
