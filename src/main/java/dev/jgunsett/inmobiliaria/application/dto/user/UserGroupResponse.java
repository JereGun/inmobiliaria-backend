package dev.jgunsett.inmobiliaria.application.dto.user;

import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Permission;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroupResponse {
	private Long id;
	private String name;
	private String description;
	private Set<Permission> permissions;
	private Set<Long> memberIds;
	private int memberCount;
}
