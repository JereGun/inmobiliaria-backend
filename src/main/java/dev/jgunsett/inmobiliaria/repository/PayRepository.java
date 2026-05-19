package dev.jgunsett.inmobiliaria.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jgunsett.inmobiliaria.domain.entity.Pay;

public interface PayRepository extends JpaRepository<Pay, Long> {
	
	List<Pay> findByInvoiceId(Long invoiceId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Pay p
            JOIN p.invoice i
            WHERE i.contract.id = :contractId
              AND p.date >= :from
              AND p.date <= :to
        """)
    BigDecimal sumAmountByContractAndDateBetween(
            @Param("contractId") Long contractId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
