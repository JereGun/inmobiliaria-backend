package dev.jgunsett.inmobiliaria.application.service;

import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.enums.Amenity;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyType;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PropertySpecification {

    private PropertySpecification() {}

    /**
     * Builds a dynamic Specification for public property filtering.
     * Only returns AVAILABLE properties (or those with null status for legacy data).
     */
    public static Specification<Property> publicFilter(
            OperationType operationType,
            PropertyType propertyType,
            Double minPrice,
            Double maxPrice,
            Integer minBedrooms,
            Integer minBathrooms,
            Set<Amenity> amenities,
            String city
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only available (or legacy null status)
            predicates.add(cb.or(
                    cb.equal(root.get("status"), PropertyStatus.AVAILABLE),
                    cb.isNull(root.get("status"))
            ));

            // Operation type (join on collection table)
            if (operationType != null) {
                Join<Object, Object> opJoin = root.join("operationTypes", JoinType.INNER);
                predicates.add(cb.equal(opJoin, operationType));
            }

            // Property type
            if (propertyType != null) {
                predicates.add(cb.equal(root.get("propertyType"), propertyType));
            }

            // Price range — check both salePrice and rentPrice depending on operation
            if (minPrice != null) {
                Predicate salePriceMin = cb.and(
                        cb.isNotNull(root.get("salePrice")),
                        cb.greaterThanOrEqualTo(root.get("salePrice"), minPrice)
                );
                Predicate rentPriceMin = cb.and(
                        cb.isNotNull(root.get("rentPrice")),
                        cb.greaterThanOrEqualTo(root.get("rentPrice"), minPrice)
                );
                predicates.add(cb.or(salePriceMin, rentPriceMin));
            }

            if (maxPrice != null) {
                Predicate salePriceMax = cb.and(
                        cb.isNotNull(root.get("salePrice")),
                        cb.lessThanOrEqualTo(root.get("salePrice"), maxPrice)
                );
                Predicate rentPriceMax = cb.and(
                        cb.isNotNull(root.get("rentPrice")),
                        cb.lessThanOrEqualTo(root.get("rentPrice"), maxPrice)
                );
                predicates.add(cb.or(salePriceMax, rentPriceMax));
            }

            // Bedrooms
            if (minBedrooms != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bedrooms"), minBedrooms));
            }

            // Bathrooms
            if (minBathrooms != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bathrooms"), minBathrooms));
            }

            // City (case-insensitive partial match)
            if (city != null && !city.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("city")),
                        "%" + city.toLowerCase() + "%"
                ));
            }

            // Amenities — each amenity is a separate join (AND logic: must have ALL)
            if (amenities != null && !amenities.isEmpty()) {
                for (Amenity amenity : amenities) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<Property> subRoot = subquery.from(Property.class);
                    Join<Object, Object> amenityJoin = subRoot.join("amenities", JoinType.INNER);
                    subquery.select(subRoot.get("id"))
                            .where(
                                    cb.equal(subRoot.get("id"), root.get("id")),
                                    cb.equal(amenityJoin, amenity)
                            );
                    predicates.add(cb.exists(subquery));
                }
            }

            // Avoid duplicate results from joins
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
