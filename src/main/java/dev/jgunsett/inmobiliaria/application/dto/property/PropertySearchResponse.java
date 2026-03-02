package dev.jgunsett.inmobiliaria.application.dto.property;

public record PropertySearchResponse(
        Long id,
        String name,
        String fullAddress,
        String ownerFullName
) {}