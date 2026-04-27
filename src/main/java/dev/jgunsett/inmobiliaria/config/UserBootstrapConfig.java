package dev.jgunsett.inmobiliaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.enums.Role;
import dev.jgunsett.inmobiliaria.repository.UserRepository;

@Configuration
public class UserBootstrapConfig {

	@Bean
	public CommandLineRunner bootstrapAdminUser(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.security.bootstrap-admin.enabled}") boolean enabled,
			@Value("${app.security.bootstrap-admin.email}") String email,
			@Value("${app.security.bootstrap-admin.password}") String password) {

		return args -> {
			if (!enabled || userRepository.count() > 0) {
				return;
			}

			User admin = User.builder()
					.email(email)
					.password(passwordEncoder.encode(password))
					.role(Role.ADMIN)
					.active(true)
					.verified(true)
					.build();

			userRepository.save(admin);
		};
	}
}
