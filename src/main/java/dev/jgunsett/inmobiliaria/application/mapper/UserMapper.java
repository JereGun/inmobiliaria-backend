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
				.displayName(request.getDisplayName())
				.whatsapp(request.getWhatsapp())
				.avatarPositionX(request.getAvatarPositionX())
				.avatarPositionY(request.getAvatarPositionY())
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
				.displayName(u.getDisplayName())
				.whatsapp(u.getWhatsapp())
				.avatarUrl(u.getAvatarUrl())
				.avatarPositionX(u.getAvatarPositionX())
				.avatarPositionY(u.getAvatarPositionY())
				.role(u.getRole())
				.active(u.getActive())
				.verified(u.getVerified())
				.createdAt(u.getCreatedAt())
				.updatedAt(u.getUpdatedAt())
				.lastLogin(u.getLastLogin())
				.groupIds(u.getGroups().stream().map(group -> group.getId()).collect(java.util.stream.Collectors.toSet()))
				.groupNames(u.getGroups().stream().map(group -> group.getName()).collect(java.util.stream.Collectors.toSet()))
				.build();
	}
}
