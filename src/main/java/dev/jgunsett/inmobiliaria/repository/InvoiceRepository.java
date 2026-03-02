package dev.jgunsett.inmobiliaria.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.Invoice;
import dev.jgunsett.inmobiliaria.domain.entity.Customer;
import dev.jgunsett.inmobiliaria.domain.entity.Contract;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceStatus;
import dev.jgunsett.inmobiliaria.domain.enums.InvoiceType;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Buscar facturas por cliente
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);

    // Buscar facturas por contrato (alquiler)
    Page<Invoice> findByContract(Contract contract, Pageable pageable);

    // Buscar facturas por estado
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    // Buscar facturas por tipo (RENT, SALE, MANUAL)
    Page<Invoice> findByType(InvoiceType type, Pageable pageable);

    // Buscar factura por código
    boolean existsByCode(String code);
}
