package dev.jgunsett.inmobiliaria.repository;

import dev.jgunsett.inmobiliaria.domain.entity.ContractEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractEventRepository extends JpaRepository<ContractEvent, Long> {

    List<ContractEvent> findByContractIdOrderByOccurredAtDesc(Long contractId);
}
