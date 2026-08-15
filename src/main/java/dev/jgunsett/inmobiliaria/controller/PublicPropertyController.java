package dev.jgunsett.inmobiliaria.controller;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertyResponse;
import dev.jgunsett.inmobiliaria.application.dto.property.PropertySearchResponse;
import dev.jgunsett.inmobiliaria.application.service.PropertyService;
import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/public/properties")
@RequiredArgsConstructor
public class PublicPropertyController {

    private final PropertyService propertyService;

    /** Lista general sin filtros (usada en la home) */
    @GetMapping
    public Page<PropertyResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return propertyService.findPublicAll(page, size);
    }

    /** Detalle de una propiedad por ID */
    @GetMapping("/detail/{id}")
    public PropertyResponse getById(@PathVariable Long id) {
        return propertyService.findPublicById(id);
    }

    /** Propiedades en venta con filtros opcionales */
    @GetMapping("/sale")
    public Page<PropertyResponse> getSale(
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Set<Amenity> amenities,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return propertyService.findPublicFiltered(
                OperationType.SALE, propertyType, minPrice, maxPrice,
                minBedrooms, minBathrooms, amenities, city, page, size);
    }

    /** Propiedades en alquiler con filtros opcionales */
    @GetMapping("/rent")
    public Page<PropertyResponse> getRent(
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minBedrooms,
            @RequestParam(required = false) Integer minBathrooms,
            @RequestParam(required = false) Set<Amenity> amenities,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return propertyService.findPublicFiltered(
                OperationType.RENT, propertyType, minPrice, maxPrice,
                minBedrooms, minBathrooms, amenities, city, page, size);
    }

    /** Búsqueda por texto */
    @GetMapping("/search")
    public Page<PropertySearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return propertyService.searchPublic(q, PageRequest.of(page, size));
    }
}
