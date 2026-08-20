package dev.jgunsett.inmobiliaria.application.dto.user;

import java.time.LocalDateTime;
import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
	
	private Long id;
	private String email;
	private String displayName;
	private String whatsapp;
	private String avatarUrl;
	private Integer avatarPositionX;
	private Integer avatarPositionY;
	private Role role;
	private Boolean active;
	private Boolean verified;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime lastLogin;
	private Set<Long> groupIds;
	private Set<String> groupNames;
}
