package dev.jgunsett.inmobiliaria.application.dto.user;

import java.util.HashSet;
import java.util.Set;

import dev.jgunsett.inmobiliaria.domain.enums.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserGroupCreateRequest {

	@NotBlank
	@Size(max = 100)
	private String name;

	@Size(max = 255)
	private String description;

	private Set<Permission> permissions = new HashSet<>();
	private Set<Long> memberIds = new HashSet<>();
}
