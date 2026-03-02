package dev.jgunsett.inmobiliaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jgunsett.inmobiliaria.application.dto.property.PropertySearchResponse;
import dev.jgunsett.inmobiliaria.domain.entity.Property;
import dev.jgunsett.inmobiliaria.domain.enums.OperationType;
import dev.jgunsett.inmobiliaria.domain.enums.PropertyStatus;

import java.util.List;


@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
	
	List<Property> findByStatus(PropertyStatus status);
	
	@EntityGraph(attributePaths = {"operationTypes","amenities"})
	List<Property> findByOwnerId(Long ownerId);
	
	Page<Property> findByStatus(
			PropertyStatus status,
			Pageable pageable
	);
	
	Page<Property> findByOperationTypes(
			OperationType operation,
			Pageable pageable
	);
	
	@Query("""
		    SELECT new dev.jgunsett.inmobiliaria.application.dto.property.PropertySearchResponse(
		        p.id,
		        p.name,
		        CONCAT(
		            COALESCE(p.street, ''), ' ',
		            COALESCE(p.numeration, ''), ' ',
		            COALESCE(p.city, ''), ' ',
		            COALESCE(p.province, '')
		        ),
		        CONCAT(
		            COALESCE(c.name, ''), ' ',
		            COALESCE(c.surname, '')
		        )
		    )
		    FROM Property p
		    JOIN p.owner c
		    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
		       OR LOWER(p.street) LIKE LOWER(CONCAT('%', :query, '%'))
		       OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
		       OR LOWER(c.surname) LIKE LOWER(CONCAT('%', :query, '%'))
		""")
	Page<PropertySearchResponse> search(@Param("query") String query, Pageable pageable);
}
