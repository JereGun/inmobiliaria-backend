package dev.jgunsett.inmobiliaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.jgunsett.inmobiliaria.domain.entity.SystemSetting;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.domain.enums.Role;
import dev.jgunsett.inmobiliaria.domain.enums.SettingType;
import dev.jgunsett.inmobiliaria.repository.SystemSettingRepository;
import dev.jgunsett.inmobiliaria.repository.UserRepository;

import java.util.List;

@Configuration
public class UserBootstrapConfig {

	@Bean
	public CommandLineRunner bootstrapAdminUser(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			SystemSettingRepository systemSettingRepository,
			@Value("${app.security.bootstrap-admin.enabled}") boolean enabled,
			@Value("${app.security.bootstrap-admin.email}") String email,
			@Value("${app.security.bootstrap-admin.password}") String password) {

		return args -> {
			if (enabled && userRepository.count() == 0) {
				User admin = User.builder()
						.email(email)
						.password(passwordEncoder.encode(password))
						.role(Role.ADMIN)
						.active(true)
						.verified(true)
						.build();
				userRepository.save(admin);
			}

			bootstrapEmailSettings(systemSettingRepository);
		};
	}

	private void bootstrapEmailSettings(SystemSettingRepository repo) {
		List<SystemSetting> defaults = List.of(
			setting("invoice.auto-generation.enabled", "false", SettingType.BOOLEAN),
			setting("email.enabled", "false", SettingType.BOOLEAN),
			setting("email.smtp.host", "smtp.example.com", SettingType.STRING),
			setting("email.smtp.port", "587", SettingType.NUMBER),
			setting("email.smtp.username", "usuario@example.com", SettingType.STRING),
			setting("email.smtp.password", "contraseña", SettingType.STRING),
			setting("email.smtp.from", "remitente@example.com", SettingType.STRING)
		);

		for (SystemSetting s : defaults) {
			if (!repo.existsByKey(s.getKey())) {
				repo.save(s);
			}
		}
	}

	private SystemSetting setting(String key, String value, SettingType type) {
		return SystemSetting.builder()
				.key(key)
				.value(value)
				.type(type)
				.editable(true)
				.build();
	}
}
