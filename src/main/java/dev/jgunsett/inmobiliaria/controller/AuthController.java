package dev.jgunsett.inmobiliaria.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.auth.AuthResponse;
import dev.jgunsett.inmobiliaria.application.dto.auth.ForgotPasswordRequest;
import dev.jgunsett.inmobiliaria.application.dto.auth.LoginRequest;
import dev.jgunsett.inmobiliaria.application.dto.auth.ResetPasswordRequest;
import dev.jgunsett.inmobiliaria.application.service.AuthService;
import dev.jgunsett.inmobiliaria.application.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

	private final AuthService authService;
	private final PasswordResetService passwordResetService;

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(Principal principal) {
		return ResponseEntity.ok(authService.me(principal.getName()));
	}

	/**
	 * Solicitar enlace de recuperación de contraseña.
	 * Siempre responde 200 para no revelar si el email existe.
	 */
	@PostMapping("/forgot-password")
	public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		passwordResetService.requestPasswordReset(request);
		return ResponseEntity.ok(Map.of("message", "Si el email está registrado, recibirás un enlace para restablecer tu contraseña"));
	}

	/**
	 * Restablecer la contraseña usando el token recibido por email.
	 */
	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		try {
			passwordResetService.resetPassword(request);
			return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente"));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
		}
	}
}
