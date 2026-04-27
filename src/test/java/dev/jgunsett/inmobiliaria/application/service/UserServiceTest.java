package dev.jgunsett.inmobiliaria.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.jgunsett.inmobiliaria.application.dto.user.UserCreateRequest;
import dev.jgunsett.inmobiliaria.application.mapper.UserMapper;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.enums.Role;
import dev.jgunsett.inmobiliaria.exception.BusinessException;
import dev.jgunsett.inmobiliaria.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private final UserMapper userMapper = new UserMapper();

	@Test
	void createHashesPasswordAndReturnsUserWithoutPassword() {
		UserCreateRequest request = new UserCreateRequest();
		request.setEmail("admin@test.com");
		request.setPassword("password123");
		request.setRole(Role.ADMIN);

		when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(1L);
			return user;
		});

		UserService service = new UserService(userRepository, passwordEncoder, userMapper);

		var response = service.create(request);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());

		assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password");
		assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
		assertThat(response.getEmail()).isEqualTo("admin@test.com");
		assertThat(response.getRole()).isEqualTo(Role.ADMIN);
	}

	@Test
	void createRejectsDuplicatedEmail() {
		UserCreateRequest request = new UserCreateRequest();
		request.setEmail("admin@test.com");
		request.setPassword("password123");
		request.setRole(Role.ADMIN);

		when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

		UserService service = new UserService(userRepository, passwordEncoder, userMapper);

		assertThatThrownBy(() -> service.create(request))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("ya se encuentra registrado");

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void updateEncodesNewPasswordWhenProvided() {
		User existing = User.builder()
				.id(1L)
				.email("user@test.com")
				.password("old-hash")
				.role(Role.USER)
				.active(true)
				.verified(false)
				.build();

		var request = new dev.jgunsett.inmobiliaria.application.dto.user.UserUpdateRequest();
		request.setPassword("new-password");

		when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

		UserService service = new UserService(userRepository, passwordEncoder, userMapper);

		service.update(1L, request);

		assertThat(existing.getPassword()).isEqualTo("new-hash");
	}
}
