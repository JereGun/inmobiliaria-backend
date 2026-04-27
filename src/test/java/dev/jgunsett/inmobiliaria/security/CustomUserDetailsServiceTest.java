package dev.jgunsett.inmobiliaria.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.enums.Permission;
import dev.jgunsett.inmobiliaria.domain.enums.Role;
import dev.jgunsett.inmobiliaria.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@Test
	void adminReceivesRoleAndAllPermissions() {
		User user = User.builder()
				.email("admin@test.com")
				.password("hash")
				.role(Role.ADMIN)
				.active(true)
				.build();

		when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

		CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

		var details = service.loadUserByUsername("admin@test.com");

		assertThat(details.getAuthorities())
				.extracting(Object::toString)
				.contains("ROLE_ADMIN", Permission.USER_READ.name(), Permission.USER_WRITE.name());
	}

	@Test
	void userDoesNotReceiveUserManagementPermissions() {
		User user = User.builder()
				.email("user@test.com")
				.password("hash")
				.role(Role.USER)
				.active(true)
				.build();

		when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

		CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

		var details = service.loadUserByUsername("user@test.com");

		assertThat(details.getAuthorities())
				.extracting(Object::toString)
				.contains("ROLE_USER", Permission.CUSTOMER_WRITE.name())
				.doesNotContain(Permission.USER_READ.name(), Permission.USER_WRITE.name());
	}
}
