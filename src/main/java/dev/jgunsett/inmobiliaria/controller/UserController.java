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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.user.UserCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserResponse;
import dev.jgunsett.inmobiliaria.application.dto.user.UserStatusUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserGroupCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.user.UserGroupResponse;
import dev.jgunsett.inmobiliaria.application.dto.user.UserGroupUpdateRequest;
import dev.jgunsett.inmobiliaria.application.service.UserService;
import dev.jgunsett.inmobiliaria.application.service.UserGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

	private final UserService userService;
	private final UserGroupService userGroupService;

	@GetMapping("/agents")
	@PreAuthorize("hasAuthority('PROPERTY_WRITE')")
	public ResponseEntity<java.util.List<UserResponse>> getAgents() {
		return ResponseEntity.ok(userService.getActiveAgents());
	}

	@GetMapping("/groups")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<java.util.List<UserGroupResponse>> getGroups() {
		return ResponseEntity.ok(userGroupService.getAll());
	}

	@PostMapping("/groups")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserGroupResponse> createGroup(@Valid @RequestBody UserGroupCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userGroupService.create(request));
	}

	@PutMapping("/groups/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserGroupResponse> updateGroup(
			@PathVariable Long id,
			@Valid @RequestBody UserGroupUpdateRequest request) {
		return ResponseEntity.ok(userGroupService.update(id, request));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
		UserResponse response = userService.create(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(response);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getAll(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(userService.getAll(page, size));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody UserUpdateRequest request) {

		return ResponseEntity.ok(userService.update(id, request));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> updateStatus(
			@PathVariable Long id,
			@Valid @RequestBody UserStatusUpdateRequest request) {

		return ResponseEntity.ok(userService.updateStatus(id, request.getActive()));
	}

	@PostMapping("/{id}/avatar")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> uploadAvatar(
			@PathVariable Long id,
			@RequestPart("file") MultipartFile file) {
		return ResponseEntity.ok(userService.uploadAvatar(id, file));
	}
}
