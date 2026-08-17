package dev.jgunsett.inmobiliaria.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.collection.OverdueCollectionResponse;
import dev.jgunsett.inmobiliaria.application.dto.collection.LateFeeRefreshResponse;
import dev.jgunsett.inmobiliaria.application.service.CollectionService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@Validated
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping("/overdue")
    public ResponseEntity<OverdueCollectionResponse> getOverdueInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int minDays,
            @RequestParam(required = false) Integer maxDays
    ) {
        return ResponseEntity.ok(collectionService.getOverdueInvoices(page, size, minDays, maxDays));
    }

    @PostMapping("/late-fees/refresh")
    public ResponseEntity<LateFeeRefreshResponse> refreshLateFees() {
        return ResponseEntity.ok(LateFeeRefreshResponse.builder()
                .date(LocalDate.now())
                .updated(collectionService.refreshLateFees())
                .build());
    }
}
