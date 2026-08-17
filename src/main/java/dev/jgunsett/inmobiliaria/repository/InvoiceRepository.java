package dev.jgunsett.inmobiliaria.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Buscar facturas por cliente
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    Page<Invoice> findByCustomerIdAndStatusIn(Long customerId, Collection<InvoiceStatus> statuses, Pageable pageable);

    long countByCustomerIdAndStatusInAndDueDateBefore(
            Long customerId,
            Collection<InvoiceStatus> statuses,
            LocalDate dueDate
    );

    java.util.Optional<Invoice> findByIdAndCustomerId(Long id, Long customerId);

    // Buscar facturas por contrato (alquiler)
    Page<Invoice> findByContract(Contract contract, Pageable pageable);

    // Buscar facturas por estado
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    // Buscar facturas por tipo (RENT, SALE, MANUAL)
    Page<Invoice> findByType(InvoiceType type, Pageable pageable);

    // Buscar factura por código
    boolean existsByCode(String code);

    List<Invoice> findByTypeAndStatusInAndDueDateBefore(
            InvoiceType type,
            Collection<InvoiceStatus> statuses,
            java.time.LocalDate dueDate
    );

    List<Invoice> findByTypeAndStatusInAndDueDateLessThanEqual(
            InvoiceType type,
            Collection<InvoiceStatus> statuses,
            LocalDate dueDate
    );

    boolean existsByContractIdAndBillingPeriod(Long contractId, String billingPeriod);

    long countByStatus(InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.total), 0) FROM Invoice i WHERE i.customer.id = :customerId AND i.status IN :statuses")
    BigDecimal sumTotalByCustomerIdAndStatusIn(
            @Param("customerId") Long customerId,
            @Param("statuses") Collection<InvoiceStatus> statuses
    );

    Page<Invoice> findByDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Invoice> findByStatusAndDateBetween(InvoiceStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
