package dev.jgunsett.inmobiliaria.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
		Set<dev.jgunsett.inmobiliaria.domain.enums.Permission> permissions = user.getRole() == dev.jgunsett.inmobiliaria.domain.enums.Role.ADMIN
				? java.util.EnumSet.allOf(dev.jgunsett.inmobiliaria.domain.enums.Permission.class)
				: user.getGroups().isEmpty()
						? user.getRole().getPermissions()
						: user.getGroups().stream()
								.flatMap(group -> group.getPermissions().stream())
								.collect(java.util.stream.Collectors.toSet());
		permissions.forEach(permission ->
				authorities.add(new SimpleGrantedAuthority(permission.name())));

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(authorities)
				.disabled(!Boolean.TRUE.equals(user.getActive()))
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.build();
	}
}
