package dev.jgunsett.inmobiliaria.application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jgunsett.inmobiliaria.application.dto.auth.AuthResponse;
import dev.jgunsett.inmobiliaria.application.dto.auth.LoginRequest;
import dev.jgunsett.inmobiliaria.application.mapper.UserMapper;
import dev.jgunsett.inmobiliaria.domain.entity.User;
import dev.jgunsett.inmobiliaria.exception.UnauthorizedException;
import dev.jgunsett.inmobiliaria.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserService userService;
	private final UserMapper userMapper;

	@Transactional
	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (DisabledException ex) {
			throw new UnauthorizedException("El usuario se encuentra inactivo");
		} catch (BadCredentialsException ex) {
			throw new UnauthorizedException("Credenciales invalidas");
		}

		User user = userService.findEntityByEmail(request.getEmail());
		userService.registerLogin(user.getEmail());
		String token = jwtService.generateToken(user);

		return AuthResponse.builder()
				.token(token)
				.tokenType("Bearer")
				.expiresIn(jwtService.getExpiration())
				.user(userMapper.toResponse(user))
				.build();
	}

	@Transactional(readOnly = true)
	public AuthResponse me(String email) {
		User user = userService.findEntityByEmail(email);

		return AuthResponse.builder()
				.tokenType("Bearer")
				.expiresIn(jwtService.getExpiration())
				.user(userMapper.toResponse(user))
				.build();
	}
}
