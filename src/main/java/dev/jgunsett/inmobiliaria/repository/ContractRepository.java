package dev.jgunsett.inmobiliaria.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

	Page<Contract> findByStatus(ContractStatus contractStatus, Pageable pageable);

	boolean existsByPropertyIdAndStatus(Long propertyId, ContractStatus status);

	List<Contract> findByOwnerId(Long ownerId);

	List<Contract> findByTenantId(Long tenantId);

	@Query("""
		SELECT c FROM Contract c
		JOIN FETCH c.property p
		JOIN FETCH c.tenant t
		WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
		   OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
		   OR LOWER(t.surname) LIKE LOWER(CONCAT('%', :query, '%'))
		   OR CAST(c.id AS string) = :query
		""")
	Page<Contract> search(@Param("query") String query, Pageable pageable);

	long countByStatus(ContractStatus status);

	@Query("SELECT COALESCE(SUM(c.baseRentalAmount), 0) FROM Contract c WHERE c.status = :status")
	BigDecimal sumBaseRentalAmountByStatus(@Param("status") ContractStatus status);

	@Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status AND c.endDate BETWEEN :from AND :to")
	long countByStatusAndEndDateBetween(
			@Param("status") ContractStatus status,
			@Param("from") LocalDate from,
			@Param("to") LocalDate to);

	Page<Contract> findByStatusAndEndDateBetween(ContractStatus status, LocalDate from, LocalDate to, Pageable pageable);
}
