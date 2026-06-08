package dev.jgunsett.inmobiliaria.application.dto.search;

public record CustomerSearchResult(
        Long id,
        String fullName,
        String documentNumber,
        String email
) {}
