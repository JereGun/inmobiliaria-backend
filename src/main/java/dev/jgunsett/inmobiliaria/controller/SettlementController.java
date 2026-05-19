package dev.jgunsett.inmobiliaria.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import dev.jgunsett.inmobiliaria.application.dto.settlement.SettlementCreateRequest;
import dev.jgunsett.inmobiliaria.application.dto.settlement.SettlementResponse;
import dev.jgunsett.inmobiliaria.application.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementResponse create(@Valid @RequestBody SettlementCreateRequest request) {
        return settlementService.create(request);
    }

    @GetMapping
    public Page<SettlementResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return settlementService.findAll(page, size);
    }

    @GetMapping("/{id}")
    public SettlementResponse findById(@PathVariable Long id) {
        return settlementService.findById(id);
    }

    @GetMapping("/owner/{ownerId}")
    public Page<SettlementResponse> findByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return settlementService.findByOwner(ownerId, page, size);
    }

    @GetMapping("/contract/{contractId}")
    public Page<SettlementResponse> findByContract(
            @PathVariable Long contractId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return settlementService.findByContract(contractId, page, size);
    }

    @GetMapping("/period/{period}")
    public Page<SettlementResponse> findByPeriod(
            @PathVariable String period,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return settlementService.findByPeriod(period, page, size);
    }

    @GetMapping("/contract/{contractId}/period/{period}")
    public SettlementResponse findByContractAndPeriod(
            @PathVariable Long contractId,
            @PathVariable String period) {

        return settlementService.findByContractAndPeriod(contractId, period);
    }
}
