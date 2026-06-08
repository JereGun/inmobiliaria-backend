package dev.jgunsett.inmobiliaria.application.service;

import dev.jgunsett.inmobiliaria.application.dto.search.ContractSearchResult;
import dev.jgunsett.inmobiliaria.application.dto.search.CustomerSearchResult;
import dev.jgunsett.inmobiliaria.application.dto.search.GlobalSearchResponse;
import dev.jgunsett.inmobiliaria.application.dto.search.PropertySearchResult;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.repository.ContractRepository;
import dev.jgunsett.inmobiliaria.repository.CustomerRepository;
import dev.jgunsett.inmobiliaria.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GlobalSearchService {

    private static final int MAX_RESULTS_PER_CATEGORY = 5;

    private final PropertyRepository propertyRepository;
    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;

    public GlobalSearchResponse search(String query) {
        if (query == null || query.trim().length() < 2) {
            return new GlobalSearchResponse(List.of(), List.of(), List.of());
        }

        String q = query.trim();
        PageRequest page = PageRequest.of(0, MAX_RESULTS_PER_CATEGORY);

        List<PropertySearchResult> properties = propertyRepository.search(q, page)
                .stream()
                .map(p -> new PropertySearchResult(p.id(), p.name(), p.fullAddress(), p.ownerFullName()))
                .toList();

        List<CustomerSearchResult> customers = customerRepository.search(q, page)
                .stream()
                .map(c -> new CustomerSearchResult(c.getId(), c.getFullName(), c.getDocumentNumber(), c.getEmail()))
                .toList();

        List<ContractSearchResult> contracts = contractRepository.search(q, page)
                .stream()
                .map(c -> new ContractSearchResult(
                        c.getId(),
                        c.getProperty().getName(),
                        c.getTenant().getFullName(),
                        c.getStatus().name(),
                        c.getContractType().name()))
                .toList();

        return new GlobalSearchResponse(properties, customers, contracts);
    }
}
