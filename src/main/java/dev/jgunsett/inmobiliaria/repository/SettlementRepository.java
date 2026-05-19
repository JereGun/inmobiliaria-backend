package dev.jgunsett.inmobiliaria.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByContractIdAndPeriod(Long contractId, String period);

    Optional<Settlement> findByContractIdAndPeriod(Long contractId, String period);

    Page<Settlement> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Settlement> findByContractId(Long contractId, Pageable pageable);

    Page<Settlement> findByPeriod(String period, Pageable pageable);
}
