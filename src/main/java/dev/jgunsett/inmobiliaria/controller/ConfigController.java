package dev.jgunsett.inmobiliaria.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.company.CompanyResponse;
import dev.jgunsett.inmobiliaria.application.dto.company.CompanyUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingResponse;
import dev.jgunsett.inmobiliaria.application.dto.setting.SystemSettingUpdateRequest;
import dev.jgunsett.inmobiliaria.application.service.CompanyService;
import dev.jgunsett.inmobiliaria.application.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Validated
public class ConfigController {

	private final CompanyService companyService;
	private final SystemSettingService systemSettingService;

	@GetMapping("/company")
	@PreAuthorize("hasAuthority('CONFIG_READ')")
	public ResponseEntity<CompanyResponse> getCompany() {
		return ResponseEntity.ok(companyService.get());
	}

	@PutMapping("/company")
	@PreAuthorize("hasAuthority('CONFIG_WRITE')")
	public ResponseEntity<CompanyResponse> updateCompany(
			@Valid @RequestBody CompanyUpdateRequest request) {

		return ResponseEntity.ok(companyService.update(request));
	}

	@PostMapping("/settings")
	@PreAuthorize("hasAuthority('CONFIG_WRITE')")
	public ResponseEntity<SystemSettingResponse> createSetting(
			@Valid @RequestBody SystemSettingCreateRequest request) {

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(systemSettingService.create(request));
	}

	@GetMapping("/settings")
	@PreAuthorize("hasAuthority('CONFIG_READ')")
	public ResponseEntity<Page<SystemSettingResponse>> getSettings(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return ResponseEntity.ok(systemSettingService.getAll(page, size));
	}

	@GetMapping("/settings/{key}")
	@PreAuthorize("hasAuthority('CONFIG_READ')")
	public ResponseEntity<SystemSettingResponse> getSettingByKey(@PathVariable String key) {
		return ResponseEntity.ok(systemSettingService.getByKey(key));
	}

	@PutMapping("/settings/{key}")
	@PreAuthorize("hasAuthority('CONFIG_WRITE')")
	public ResponseEntity<SystemSettingResponse> updateSetting(
			@PathVariable String key,
			@Valid @RequestBody SystemSettingUpdateRequest request) {

		return ResponseEntity.ok(systemSettingService.update(key, request));
	}
}
