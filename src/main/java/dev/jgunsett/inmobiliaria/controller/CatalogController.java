package dev.jgunsett.inmobiliaria.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jgunsett.inmobiliaria.application.dto.CatalogItemResponse;
import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;

@RestController
@RequestMapping("/api/v1/catalogs")
public class CatalogController {
	
    @GetMapping("/amenities")
    public List<CatalogItemResponse> getAll() {
        return Arrays.stream(Amenity.values())
                .map(a -> new CatalogItemResponse(
                        a.name(),
                        a.getDescription()
                ))
                .toList();
    }

    @GetMapping("/property-status")
    public List<CatalogItemResponse> propertyStatus() {
        return Arrays.stream(PropertyStatus.values())
                .map(s -> new CatalogItemResponse(
                        s.name(),
                        s.getDescription()
                ))
                .toList();
    }

    @GetMapping("/property-types")
    public List<CatalogItemResponse> propertyTypes() {
        return Arrays.stream(PropertyType.values())
                .map(t -> new CatalogItemResponse(
                        t.name(),
                        t.getDescription()
                ))
                .toList();
    }

    @GetMapping("/operation-types")
    public List<CatalogItemResponse> operationTypes() {
        return Arrays.stream(OperationType.values())
                .map(o -> new CatalogItemResponse(
                        o.name(),
                        o.getDescription()
                ))
                .toList();
    }
}