package dev.jgunsett.inmobiliaria.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.user.UserCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserResponse;
import dev.jgunsett.inmobiliaria.application.dto.user.UserStatusUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserUpdateRequest;
import dev.jgunsett.inmobiliaria.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
		UserResponse response = userService.create(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}

	@GetMapping
	public ResponseEntity<Page<UserResponse>> getAll(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(userService.getAll(page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody UserUpdateRequest request) {

		return ResponseEntity.ok(userService.update(id, request));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<UserResponse> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UserStatusUpdateRequest request) {

		return ResponseEntity.ok(userService.updateStatus(id, request.getActive()));
	}
}
