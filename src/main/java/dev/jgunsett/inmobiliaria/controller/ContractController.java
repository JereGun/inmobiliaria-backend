package dev.jgunsett.inmobiliaria.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractAdjustmentCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractAdjustmentResponse;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractResponse;
import dev.jgunsett.inmobiliaria.application.dto.contract.ContractUpdateRequest;
import dev.jgunsett.inmobiliaria.application.service.ContractService;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {
	
	private final ContractService contractService;
	
	@GetMapping
	public Page<ContractResponse> getAll(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size
	) {
		return contractService.findAll(page, size);
	}
	
	@GetMapping("/{id}")
	public ContractResponse getById(@PathVariable Long id) {
		return contractService.findById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ContractResponse create(@RequestBody @Valid ContractCreateRequest request) {
		return contractService.create(request);
	}
	
	@PutMapping("/{id}")
	public ContractResponse update (@PathVariable Long id, @RequestBody ContractUpdateRequest request) {
		return contractService.update(id, request);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		contractService.delete(id);
	}
	
	@GetMapping("/status")
	public Page<ContractResponse> getByStatus(
	        @RequestParam ContractStatus status,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size
	) {
	    return contractService.findByStatus(status, page, size);
	}
	
	@PostMapping("/{id}/activate")
	public ResponseEntity<ContractResponse> activate(@PathVariable Long id) {
	    return ResponseEntity.ok(contractService.activate(id));
	}

	@PostMapping("/{id}/suspend")
	public ResponseEntity<ContractResponse> suspend(@PathVariable Long id) {
	    return ResponseEntity.ok(contractService.suspend(id));
	}

	@PostMapping("/{id}/resume")
	public ResponseEntity<ContractResponse> resume(@PathVariable Long id) {
	    return ResponseEntity.ok(contractService.resume(id));
	}

	@PostMapping("/{id}/finish")
	public ResponseEntity<ContractResponse> finish(@PathVariable Long id) {
	    return ResponseEntity.ok(contractService.finish(id));
	}

	@PostMapping("/{id}/terminate")
	public ResponseEntity<ContractResponse> terminate(@PathVariable Long id) {
	    return ResponseEntity.ok(contractService.terminate(id));
	}

	@GetMapping("/{id}/adjustments")
	public ResponseEntity<List<ContractAdjustmentResponse>> getAdjustments(@PathVariable Long id) {
	    return ResponseEntity.ok(contractService.getAdjustments(id));
	}

	@PostMapping("/{id}/adjustments")
	@ResponseStatus(HttpStatus.CREATED)
	public ContractAdjustmentResponse addAdjustment(
	        @PathVariable Long id,
	        @Valid @RequestBody ContractAdjustmentCreateRequest request) {

	    return contractService.addAdjustment(id, request);
	}

	@GetMapping("/{id}/rental-amount")
	public ResponseEntity<BigDecimal> calculateRentalAmount(
	        @PathVariable Long id,
	        @RequestParam(required = false) LocalDate date) {

	    LocalDate calculationDate = date != null ? date : LocalDate.now();

	    return ResponseEntity.ok(contractService.calculateRentalAmount(id, calculationDate));
	}
	
}
