package dev.jgunsett.inmobiliaria.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.enums.ContractStatus;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
	
	Page<Contract> findByStatus(ContractStatus contractStatus, Pageable pageable);
	
	boolean existsByPropertyIdAndStatus(Long propertyId, ContractStatus status);
	
	List<Contract> findByOwnerId(Long ownerId);
	
	List<Contract> findByTenantId(Long tenantId);
}
