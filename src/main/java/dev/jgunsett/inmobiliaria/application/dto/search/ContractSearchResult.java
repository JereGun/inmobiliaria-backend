package dev.jgunsett.inmobiliaria.application.dto.search;

public record ContractSearchResult(
        Long id,
        String propertyName,
        String tenantFullName,
        String status,
        String contractType
) {}
