package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jgunsett.inmobiliaria.domain.entity.ContractAdjustment;

public interface ContractAdjustmentRepository extends JpaRepository<ContractAdjustment, Long> {
	
	// Ajuste activo vigente para una fecha
	@Query("""
	SELECT ca
	FROM ContractAdjustment ca
	WHERE ca.contract.id = :contractId
	  AND ca.active = true
	  AND ca.effectiveDate <= :date
	ORDER BY ca.effectiveDate DESC
	""")
	List<ContractAdjustment> findActiveAdjustmentsUpToDate(
	    @Param("contractId") Long contractId,
	    @Param("date") LocalDate date
	);
	
	List<ContractAdjustment> findByContractId(Long contractId);
	
    boolean existsByContractIdAndEffectiveDate(Long contractId, LocalDate effectiveDate);
    
}