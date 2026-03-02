package dev.jgunsett.inmobiliaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.PropertyImage;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    List<PropertyImage> findByPropertyId(Long propertyId);

    Optional<PropertyImage> findByPropertyIdAndCoverTrue(Long propertyId);
}
