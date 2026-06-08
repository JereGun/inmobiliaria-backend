package dev.jgunsett.inmobiliaria.application.dto.search;

public record PropertySearchResult(
        Long id,
        String name,
        String fullAddress,
        String ownerFullName
) {}
