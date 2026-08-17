package dev.jgunsett.inmobiliaria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.jgunsett.inmobiliaria.domain.entity.InvoiceDelivery;

public interface InvoiceDeliveryRepository extends JpaRepository<InvoiceDelivery, Long> {

    Optional<InvoiceDelivery> findByInvoiceIdAndChannel(Long invoiceId, String channel);

    List<InvoiceDelivery> findByInvoiceIdOrderByCreationDateDesc(Long invoiceId);
}
