package dev.jgunsett.inmobiliaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.enums.DocumentType;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	
	Optional<Customer> findByDocumentTypeAndDocumentNumber(
			DocumentType documentType,
			String documentNumber
	);
	
	Optional<Customer> findByEmail(String email);
	
	boolean existsByDocumentTypeAndDocumentNumber(
			DocumentType documentType,
			String documentNumber
	);
	
	boolean existsByEmail(String email);
	
	@Query("""
		    SELECT DISTINCT c
		    FROM Customer c
		    JOIN Property p ON p.owner.id = c.id
		""")
	Page<Customer> findOwners(Pageable pageable);
	
	@Query("""
		    SELECT c FROM Customer c
		    WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
		       OR LOWER(c.surname) LIKE LOWER(CONCAT('%', :query, '%'))
		       OR c.documentNumber LIKE CONCAT('%', :query, '%')
		""")
	Page<Customer> search(@Param("query") String query, Pageable pageable);

}
