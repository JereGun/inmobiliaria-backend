package dev.jgunsett.inmobiliaria.application.dto.search;

import java.util.List;

public record GlobalSearchResponse(
        List<PropertySearchResult> properties,
        List<CustomerSearchResult> customers,
        List<ContractSearchResult> contracts
) {}
