package dev.jgunsett.inmobiliaria.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import dev.jgunsett.inmobiliaria.application.dto.contract.ContractResponse;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerResponse;
import dev.jgunsett.inmobiliaria.application.dto.customer.CustomerUpdateRequest;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertyResponse;
import dev.jgunsett.inmobiliaria.application.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {

    private final CustomerService customerService;

    // Crear Customer
    @PostMapping
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CustomerCreateRequest request) {

        CustomerResponse response = customerService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Modificar Customer
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {

        CustomerResponse response = customerService.update(id, request);

        return ResponseEntity.ok(response);
    }

    // Buscar Customer por ID
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(
            @PathVariable Long id) {

        CustomerResponse response = customerService.getById(id);

        return ResponseEntity.ok(response);
    }

    // Listar Customers (paginado)
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CustomerResponse> response = customerService.getAll(page, size);

        return ResponseEntity.ok(response);
    }

    // Eliminar Customer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        customerService.delete(id);

        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/contracts")
    public ResponseEntity<List<ContractResponse>> getContracts(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerContracts(id));
    }
    
    @GetMapping("/{id}/properties")
    public ResponseEntity<List<PropertyResponse>> getProperties(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerProperties(id));
    }

    @GetMapping("/owners")
    public Page<CustomerResponse> getOwners(Pageable pageable) {
        return customerService.findOwners(pageable);
    }
    
    @GetMapping("/search")
    public Page<CustomerResponse> search(
            @RequestParam String query,
            Pageable pageable) {

        return customerService.search(query, pageable);
    }
}
